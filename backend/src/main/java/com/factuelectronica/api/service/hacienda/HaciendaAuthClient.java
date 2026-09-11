package com.factuelectronica.api.service.hacienda;

import com.factuelectronica.api.config.HaciendaProperties;
import com.factuelectronica.api.exception.HaciendaIntegrationException;
import com.factuelectronica.api.model.Ambiente;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

/**
 * Obtiene y cachea el token de acceso OAuth2/OpenID Connect del IdP de Hacienda,
 * usando el flujo "password" con las credenciales ATV del emisor (usuario y
 * contraseña obtenidas del portal ATV, asociadas a su llave criptográfica).
 *
 * El cacheo real por emisor/ambiente debe hacerse contra la tabla
 * token_oauth_cache (o Redis) respetando expira_en; aquí se ilustra únicamente
 * la llamada HTTP.
 */
@Service
@RequiredArgsConstructor
public class HaciendaAuthClient {

    private final WebClient.Builder webClientBuilder;
    private final HaciendaProperties properties;

    public TokenHacienda obtenerToken(Ambiente ambiente, String usuarioAtv, String passwordAtv) {
        HaciendaProperties.AmbienteConfig config = configPara(ambiente);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", config.getClientId());
        form.add("username", usuarioAtv);
        form.add("password", passwordAtv);

        try {
            return webClientBuilder.build()
                    .post()
                    .uri(config.getAuthUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(form)
                    .retrieve()
                    .bodyToMono(TokenHacienda.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();
        } catch (WebClientResponseException e) {
            boolean reintentable = e.getStatusCode().is5xxServerError();
            throw new HaciendaIntegrationException(
                    "Error al obtener token OAuth2 de Hacienda: " + e.getStatusCode(), e, reintentable);
        }
    }

    private HaciendaProperties.AmbienteConfig configPara(Ambiente ambiente) {
        String clave = ambiente == Ambiente.PRODUCCION ? "produccion" : "sandbox";
        HaciendaProperties.AmbienteConfig config = properties.getAmbientes().get(clave);
        if (config == null) {
            throw new IllegalStateException("No hay configuración de Hacienda para el ambiente: " + clave);
        }
        return config;
    }

    /** DTO de la respuesta del endpoint de token del IdP de Hacienda. */
    public record TokenHacienda(
            String access_token,
            String refresh_token,
            long expires_in,
            long refresh_expires_in,
            String token_type
    ) {}
}
