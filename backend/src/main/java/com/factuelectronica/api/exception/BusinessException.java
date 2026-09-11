package com.factuelectronica.api.exception;

/** Error de regla de negocio (equivalente a 422 Unprocessable Entity). */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
