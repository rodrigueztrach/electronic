package com.factuelectronica.api.dto;

import com.factuelectronica.api.model.Comprobante;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ComprobanteResponse(
        UUID id,
        String claveNumerica,
        String consecutivo,
        String tipoDocumento,
        String estado,
        OffsetDateTime fechaEmision,
        String moneda,
        BigDecimal totalComprobante,
        String motivoError
) {
    public static ComprobanteResponse desde(Comprobante c) {
        return new ComprobanteResponse(
                c.getId(),
                c.getClaveNumerica(),
                c.getConsecutivo(),
                c.getTipoDocumento().getCodigo(),
                c.getEstado().name().toLowerCase(),
                c.getFechaEmision(),
                c.getMoneda(),
                c.getTotalComprobante(),
                c.getMotivoError()
        );
    }
}
