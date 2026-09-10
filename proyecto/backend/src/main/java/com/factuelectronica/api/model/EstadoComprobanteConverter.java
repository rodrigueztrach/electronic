package com.factuelectronica.api.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Persiste el enum EstadoComprobante en minúsculas (ej. 'aceptado_parcial') como en la BD. */
@Converter(autoApply = true)
public class EstadoComprobanteConverter implements AttributeConverter<EstadoComprobante, String> {

    @Override
    public String convertToDatabaseColumn(EstadoComprobante attribute) {
        return attribute == null ? null : attribute.name().toLowerCase();
    }

    @Override
    public EstadoComprobante convertToEntityAttribute(String dbData) {
        return dbData == null ? null : EstadoComprobante.valueOf(dbData.toUpperCase());
    }
}
