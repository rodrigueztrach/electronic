package com.factuelectronica.api.service;

import com.factuelectronica.api.dto.LoginRequest;
import com.factuelectronica.api.dto.LoginResponse;
import com.factuelectronica.api.dto.RegistroUsuarioRequest;
import com.factuelectronica.api.exception.BusinessException;
import com.factuelectronica.api.exception.InvalidCredentialsException;
import com.factuelectronica.api.model.Usuario;
import com.factuelectronica.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(request.correo())
                .orElseThrow(() -> new InvalidCredentialsException("Correo o contraseña incorrectos"));

        if (!"activo".equals(usuario.getEstado())) {
            throw new InvalidCredentialsException("La cuenta de usuario está inactiva");
        }

        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            throw new InvalidCredentialsException("Correo o contraseña incorrectos");
        }

        String token = jwtService.generarToken(usuario);
        return new LoginResponse(token, "Bearer", jwtService.expiracionSegundos(),
                usuario.getCorreo(), usuario.getNombreCompleto());
    }

    @Transactional
    public LoginResponse registrar(RegistroUsuarioRequest request) {
        if (usuarioRepository.existsByCorreo(request.correo())) {
            throw new BusinessException("Ya existe un usuario registrado con ese correo");
        }

        Usuario usuario = Usuario.builder()
                .correo(request.correo())
                .nombreCompleto(request.nombreCompleto())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();
        usuario = usuarioRepository.save(usuario);

        String token = jwtService.generarToken(usuario);
        return new LoginResponse(token, "Bearer", jwtService.expiracionSegundos(),
                usuario.getCorreo(), usuario.getNombreCompleto());
    }
}