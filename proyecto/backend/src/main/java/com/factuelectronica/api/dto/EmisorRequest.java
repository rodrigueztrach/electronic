package com.factuelectronica.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EmisorRequest(
        @NotBlank @Pattern(regexp = "0[1-6]", message = "Tipo de identificación inválido") String tipoIdentificacion,
        @NotBlank @Pattern(regexp = "[0-9A-Za-z-]{9,20}") String identificacion,
        @NotBlank String razonSocial,
        String nombreComercial,
        @NotBlank @Pattern(regexp = "\\d{4,6}") String actividadEconomica,
        @Email String correoNotificacion,
        @NotBlank @Pattern(regexp = "sandbox|produccion") String ambiente,
        String proveedorSistemas
) {}
