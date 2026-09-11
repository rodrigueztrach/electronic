package com.factuelectronica.api.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String correo,
        String nombreCompleto
) {}