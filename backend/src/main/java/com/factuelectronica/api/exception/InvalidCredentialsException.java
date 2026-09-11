package com.factuelectronica.api.exception;

/** Correo/contraseña incorrectos, o cuenta inactiva. */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}