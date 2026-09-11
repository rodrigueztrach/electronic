package com.factuelectronica.api.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Persiste el enum TipoDocumento como el código de 2 dígitos usado por Hacienda (ej. "01"). */
@Converter(autoApply = true)
public class TipoDocumentoConverter implements AttributeConverter<TipoDocumento, String> {

    @Override
    public String convertToDatabaseColumn(TipoDocumento attribute) {
        return attribute == null ? null : attribute.getCodigo();
    }

    @Override
    public TipoDocumento convertToEntityAttribute(String dbData) {
        return dbData == null ? null : TipoDocumento.desdeCodigo(dbData);
    }
}
