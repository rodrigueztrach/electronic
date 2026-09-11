package com.factuelectronica.api.controller;

import com.factuelectronica.api.dto.LoginRequest;
import com.factuelectronica.api.dto.LoginResponse;
import com.factuelectronica.api.dto.RegistroUsuarioRequest;
import com.factuelectronica.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Login y registro de usuarios internos")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Inicia sesión y devuelve un token de acceso (JWT)")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/registro")
    @Operation(summary = "Registra un nuevo usuario interno y devuelve un token de acceso")
    public ResponseEntity<LoginResponse> registrar(@Valid @RequestBody RegistroUsuarioRequest request) {
        // NOTA DE SEGURIDAD: este endpoint queda abierto (permitAll) solo para
        // facilitar el desarrollo local. En producción debe protegerse (por
        // ejemplo, exigiendo rol ADMIN o deshabilitando el auto-registro).
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }
}