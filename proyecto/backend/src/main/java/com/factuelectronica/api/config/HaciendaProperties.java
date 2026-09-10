package com.factuelectronica.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "facturacion.hacienda")
public class HaciendaProperties {

    private Map<String, AmbienteConfig> ambientes;
    private Reintentos reintentos = new Reintentos();

    public Map<String, AmbienteConfig> getAmbientes() {
        return ambientes;
    }

    public void setAmbientes(Map<String, AmbienteConfig> ambientes) {
        this.ambientes = ambientes;
    }

    public Reintentos getReintentos() {
        return reintentos;
    }

    public void setReintentos(Reintentos reintentos) {
        this.reintentos = reintentos;
    }

    public static class AmbienteConfig {
        private String authUrl;
        private String apiUrl;
        private String clientId;

        public String getAuthUrl() { return authUrl; }
        public void setAuthUrl(String authUrl) { this.authUrl = authUrl; }
        public String getApiUrl() { return apiUrl; }
        public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }
    }

    public static class Reintentos {
        private int maxIntentos = 5;
        private int backoffBaseSegundos = 60;

        public int getMaxIntentos() { return maxIntentos; }
        public void setMaxIntentos(int maxIntentos) { this.maxIntentos = maxIntentos; }
        public int getBackoffBaseSegundos() { return backoffBaseSegundos; }
        public void setBackoffBaseSegundos(int backoffBaseSegundos) { this.backoffBaseSegundos = backoffBaseSegundos; }
    }
}
