package com.factuelectronica.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductoRequest(
        @NotNull UUID emisorId,
        String codigoInterno,
        @NotBlank @Pattern(regexp = "\\d{13}") String codigoCabys,
        @NotBlank String nombre,
        @NotBlank String unidadMedida,
        @NotNull BigDecimal precioUnitario,
        BigDecimal tarifaImpuesto,
        String registroMedicamento,
        String formaFarmaceutica
) {}
