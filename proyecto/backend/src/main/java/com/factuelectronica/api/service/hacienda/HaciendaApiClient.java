package com.factuelectronica.api.service.hacienda;

import com.factuelectronica.api.config.HaciendaProperties;
import com.factuelectronica.api.exception.HaciendaIntegrationException;
import com.factuelectronica.api.model.Ambiente;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Map;

/**
 * Cliente del API REST de Recepción de Comprobantes Electrónicos del
 * Ministerio de Hacienda (POST /recepcion y GET /recepcion/{clave}).
 */
@Service
@RequiredArgsConstructor
public class HaciendaApiClient {

    private final WebClient.Builder webClientBuilder;
    private final HaciendaProperties properties;

    public EnvioResultado enviarComprobante(Ambiente ambiente,
                                             String accessToken,
                                             String clave,
                                             OffsetDateTime fechaEmision,
                                             String tipoIdentificacionEmisor,
                                             String identificacionEmisor,
                                             String tipoIdentificacionReceptor,
                                             String identificacionReceptor,
                                             String xmlFirmado) {

        String apiUrl = apiUrlPara(ambiente);
        String xmlBase64 = Base64.getEncoder().encodeToString(xmlFirmado.getBytes());

        Map<String, Object> body = Map.of(
                "clave", clave,
                "fecha", fechaEmision.toString(),
                "emisor", Map.of(
                        "tipoIdentificacion", tipoIdentificacionEmisor,
                        "numeroIdentificacion", identificacionEmisor),
                "receptor", identificacionReceptor == null ? Map.of() : Map.of(
                        "tipoIdentificacion", tipoIdentificacionReceptor,
                        "numeroIdentificacion", identificacionReceptor),
                "comprobanteXml", xmlBase64
        );

        try {
            webClientBuilder.build()
                    .post()
                    .uri(apiUrl + "/recepcion")
                    .headers(h -> h.setBearerAuth(accessToken))
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofSeconds(20))
                    .block();
            return new EnvioResultado(true, "recibido", null);

        } catch (WebClientResponseException e) {
            HttpStatusCode status = e.getStatusCode();
            if (status.value() == 409) {
                // Comprobante ya recibido con esa clave: se trata como éxito idempotente.
                return new EnvioResultado(true, "recibido_duplicado", null);
            }
            boolean reintentable = status.value() == 429 || status.is5xxServerError();
            if (!reintentable) {
                // 400 / 401 / 403: error definitivo, requiere intervención.
                return new EnvioResultado(false, "error", e.getResponseBodyAsString());
            }
            throw new HaciendaIntegrationException(
                    "Error transitorio al enviar comprobante a Hacienda: " + status, e, true);
        }
    }

    public ConsultaResultado consultarEstado(Ambiente ambiente, String accessToken, String clave) {
        String apiUrl = apiUrlPara(ambiente);
        try {
            Map<?, ?> respuesta = webClientBuilder.build()
                    .get()
                    .uri(apiUrl + "/recepcion/" + clave)
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();

            String indEstado = respuesta != null ? String.valueOf(respuesta.get("ind-estado")) : "desconocido";
            String xmlRespuesta = respuesta != null && respuesta.get("respuesta-xml") != null
                    ? new String(Base64.getDecoder().decode(String.valueOf(respuesta.get("respuesta-xml"))))
                    : null;
            return new ConsultaResultado(indEstado, xmlRespuesta);

        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return new ConsultaResultado("no_encontrado", null);
            }
            boolean reintentable = e.getStatusCode().is5xxServerError();
            throw new HaciendaIntegrationException(
                    "Error al consultar estado en Hacienda: " + e.getStatusCode(), e, reintentable);
        }
    }

    private String apiUrlPara(Ambiente ambiente) {
        String clave = ambiente == Ambiente.PRODUCCION ? "produccion" : "sandbox";
        HaciendaProperties.AmbienteConfig config = properties.getAmbientes().get(clave);
        if (config == null) {
            throw new IllegalStateException("No hay configuración de Hacienda para el ambiente: " + clave);
        }
        return config.getApiUrl();
    }

    public record EnvioResultado(boolean exitoso, String estado, String detalleError) {}

    public record ConsultaResultado(String indEstado, String xmlRespuesta) {}
}
