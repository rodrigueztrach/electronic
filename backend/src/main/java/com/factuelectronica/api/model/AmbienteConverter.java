package com.factuelectronica.api.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Persiste el enum Ambiente como 'sandbox' | 'produccion' (coincide con el CHECK de la BD). */
@Converter(autoApply = true)
public class AmbienteConverter implements AttributeConverter<Ambiente, String> {

    @Override
    public String convertToDatabaseColumn(Ambiente attribute) {
        if (attribute == null) return null;
        return attribute == Ambiente.SANDBOX ? "sandbox" : "produccion";
    }

    @Override
    public Ambiente convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return "produccion".equalsIgnoreCase(dbData) ? Ambiente.PRODUCCION : Ambiente.SANDBOX;
    }
}
