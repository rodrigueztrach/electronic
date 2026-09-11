package com.factuelectronica.api.dto;

import com.factuelectronica.api.model.ProductoServicio;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductoResponse(
        UUID id,
        String codigoInterno,
        String codigoCabys,
        String nombre,
        String unidadMedida,
        BigDecimal precioUnitario,
        BigDecimal tarifaImpuesto,
        String estado
) {
    public static ProductoResponse desde(ProductoServicio p) {
        return new ProductoResponse(
                p.getId(),
                p.getCodigoInterno(),
                p.getCodigoCabys(),
                p.getNombre(),
                p.getUnidadMedida(),
                p.getPrecioUnitario(),
                p.getTarifaImpuesto(),
                p.getEstado()
        );
    }
}
