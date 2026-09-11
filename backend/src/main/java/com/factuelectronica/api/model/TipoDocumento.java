package com.factuelectronica.api.model;

/**
 * Tipos de comprobante electrónico soportados por la versión 4.4
 * de los Anexos y Estructuras del Ministerio de Hacienda.
 */
public enum TipoDocumento {
    FACTURA_ELECTRONICA("01"),
    NOTA_DEBITO("02"),
    NOTA_CREDITO("03"),
    TIQUETE_ELECTRONICO("04"),
    FACTURA_ELECTRONICA_COMPRA("08"),
    FACTURA_ELECTRONICA_EXPORTACION("09"),
    RECIBO_ELECTRONICO_PAGO("10");

    private final String codigo;

    TipoDocumento(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    public static TipoDocumento desdeCodigo(String codigo) {
        for (TipoDocumento t : values()) {
            if (t.codigo.equals(codigo)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Código de tipo de documento inválido: " + codigo);
    }
}
