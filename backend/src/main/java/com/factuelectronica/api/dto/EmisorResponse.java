package com.factuelectronica.api.dto;

import com.factuelectronica.api.model.Emisor;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EmisorResponse(
        UUID id,
        String tipoIdentificacion,
        String identificacion,
        String razonSocial,
        String nombreComercial,
        String actividadEconomica,
        String ambiente,
        String versionEsquema,
        String estado,
        OffsetDateTime creadoEn
) {
    public static EmisorResponse desde(Emisor e) {
        return new EmisorResponse(
                e.getId(),
                e.getTipoIdentificacion(),
                e.getIdentificacion(),
                e.getRazonSocial(),
                e.getNombreComercial(),
                e.getActividadEconomica(),
                e.getAmbiente().name().toLowerCase(),
                e.getVersionEsquema(),
                e.getEstado(),
                e.getCreadoEn()
        );
    }
}
