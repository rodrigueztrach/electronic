package com.factuelectronica.api.model;

/**
 * Ciclo de vida de un comprobante dentro del sistema propio (no confundir
 * con el "ind-estado" devuelto por el API de Hacienda, ver MensajeHacienda).
 */
public enum EstadoComprobante {
    CREADO,
    VALIDADO,
    FIRMADO,
    ENVIADO,
    ACEPTADO,
    ACEPTADO_PARCIAL,
    RECHAZADO,
    ERROR_ENVIO,
    ANULADO
}
