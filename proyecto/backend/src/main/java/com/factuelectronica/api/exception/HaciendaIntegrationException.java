package com.factuelectronica.api.exception;

/** Error transitorio o definitivo al comunicarse con el API/IdP de Hacienda. */
public class HaciendaIntegrationException extends RuntimeException {

    private final boolean reintentable;

    public HaciendaIntegrationException(String message, boolean reintentable) {
        super(message);
        this.reintentable = reintentable;
    }

    public HaciendaIntegrationException(String message, Throwable cause, boolean reintentable) {
        super(message, cause);
        this.reintentable = reintentable;
    }

    public boolean isReintentable() {
        return reintentable;
    }
}
