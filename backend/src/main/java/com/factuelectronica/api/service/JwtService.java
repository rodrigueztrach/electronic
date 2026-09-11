package com.factuelectronica.api.service;

import com.factuelectronica.api.model.Usuario;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Emite JWT firmados con HMAC-SHA256 usando una clave secreta compartida
 * (configurada en `facturacion.seguridad.jwt-secret`). El mismo secreto se usa
 * en SecurityConfig para validar los tokens entrantes en cada request.
 *
 * Este es un emisor de tokens "propio", pensado para desarrollo y para
 * instalaciones que no requieren un proveedor de identidad externo. Si más
 * adelante se integra Keycloak u otro IdP, este servicio se reemplaza por la
 * configuración estándar de `issuer-uri` sin afectar el resto de la aplicación,
 * ya que los controladores solo dependen de que exista un JWT válido.
 */
@Service
public class JwtService {

    private final byte[] secretBytes;
    private final long expiracionMinutos;

    public JwtService(
            @Value("${facturacion.seguridad.jwt-secret}") String secret,
            @Value("${facturacion.seguridad.jwt-expiracion-minutos:60}") long expiracionMinutos) {
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.expiracionMinutos = expiracionMinutos;
        if (this.secretBytes.length < 32) {
            throw new IllegalStateException(
                    "facturacion.seguridad.jwt-secret debe tener al menos 32 caracteres (256 bits) para HS256");
        }
    }

    public String generarToken(Usuario usuario) {
        try {
            Instant ahora = Instant.now();
            Instant expira = ahora.plus(expiracionMinutos, ChronoUnit.MINUTES);

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(usuario.getId().toString())
                    .claim("correo", usuario.getCorreo())
                    .claim("nombre", usuario.getNombreCompleto())
                    .issueTime(Date.from(ahora))
                    .expirationTime(Date.from(expira))
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            signedJWT.sign(new MACSigner(secretBytes));
            return signedJWT.serialize();

        } catch (Exception e) {
            throw new IllegalStateException("Error al generar el token de acceso: " + e.getMessage(), e);
        }
    }

    public long expiracionSegundos() {
        return expiracionMinutos * 60;
    }
}