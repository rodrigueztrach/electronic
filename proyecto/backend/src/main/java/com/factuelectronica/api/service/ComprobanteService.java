package com.factuelectronica.api.service;

import com.factuelectronica.api.dto.ComprobanteRequest;
import com.factuelectronica.api.dto.ComprobanteResponse;
import com.factuelectronica.api.exception.BusinessException;
import com.factuelectronica.api.exception.HaciendaIntegrationException;
import com.factuelectronica.api.exception.ResourceNotFoundException;
import com.factuelectronica.api.model.*;
import com.factuelectronica.api.repository.ComprobanteRepository;
import com.factuelectronica.api.repository.EmisorRepository;
import com.factuelectronica.api.repository.ReceptorRepository;
import com.factuelectronica.api.service.hacienda.HaciendaApiClient;
import com.factuelectronica.api.service.hacienda.HaciendaAuthClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Orquesta el ciclo de vida completo de un comprobante:
 * validar -> generar clave/consecutivo -> construir XML -> firmar -> persistir
 * -> enviar a Hacienda -> registrar respuesta.
 *
 * NOTA: el envío real a Hacienda requiere el certificado .p12 y las credenciales
 * ATV del emisor (ver CertificadoCriptografico / CredencialHacienda en el
 * esquema de base de datos). Aquí se muestra el flujo completo con los puntos
 * de extensión marcados como TODO para mantener el ejemplo enfocado.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComprobanteService {

    private final ComprobanteRepository comprobanteRepository;
    private final EmisorRepository emisorRepository;
    private final ReceptorRepository receptorRepository;

    private final ClaveNumericaService claveNumericaService;
    private final ComprobanteXmlService xmlService;
    private final XmlSignerService signerService;
    private final HaciendaAuthClient haciendaAuthClient;
    private final HaciendaApiClient haciendaApiClient;

    @Transactional
    public ComprobanteResponse crearYEmitir(ComprobanteRequest request) {
        Emisor emisor = emisorRepository.findById(request.emisorId())
                .orElseThrow(() -> new ResourceNotFoundException("Emisor no encontrado: " + request.emisorId()));

        Receptor receptor = resolverReceptor(emisor, request.receptor());

        TipoDocumento tipoDocumento = TipoDocumento.desdeCodigo(request.tipoDocumento());
        OffsetDateTime fechaEmision = OffsetDateTime.now();

        String consecutivo = claveNumericaService.generarConsecutivo("001", "00001", tipoDocumento);
        String claveNumerica = claveNumericaService.generarClaveNumerica(
                emisor.getIdentificacion(), consecutivo, fechaEmision, emisor.getAmbiente());

        Comprobante comprobante = Comprobante.builder()
                .emisor(emisor)
                .receptor(receptor)
                .tipoDocumento(tipoDocumento)
                .claveNumerica(claveNumerica)
                .consecutivo(consecutivo)
                .fechaEmision(fechaEmision)
                .moneda(request.moneda())
                .tipoCambio(request.tipoCambio())
                .condicionVenta(request.condicionVenta())
                .plazoCredito(request.plazoCredito())
                .proveedorSistemas(request.proveedorSistemas() != null
                        ? request.proveedorSistemas() : emisor.getProveedorSistemas())
                .versionEsquema(emisor.getVersionEsquema())
                .build();

        agregarLineasDetalle(comprobante, request);
        calcularTotales(comprobante);

        comprobante.setEstado(EstadoComprobante.VALIDADO);
        comprobante = comprobanteRepository.save(comprobante);

        // Generación de XML
        String xmlSinFirmar = xmlService.construirXml(comprobante);

        // TODO: obtener el certificado .p12 y contraseña reales desde
        // CertificadoCriptografico (desencriptados en memoria) para el emisor.
        // A modo de ejemplo se omite la firma real si no hay certificado configurado.
        try {
            byte[] certificadoDemo = obtenerCertificadoDelEmisor(emisor);
            char[] passwordDemo = obtenerPasswordDelEmisor(emisor);
            String xmlFirmado = signerService.firmar(xmlSinFirmar, certificadoDemo, passwordDemo);
            comprobante.setXmlFirmado(xmlFirmado);
            comprobante.setEstado(EstadoComprobante.FIRMADO);
        } catch (Exception e) {
            log.warn("No fue posible firmar el comprobante {}: {}", claveNumerica, e.getMessage());
            comprobante.setXmlFirmado(xmlSinFirmar); // fallback solo para fines demostrativos
        }

        comprobante = comprobanteRepository.save(comprobante);

        enviarAHacienda(comprobante);

        return ComprobanteResponse.desde(comprobante);
    }

    @Transactional
    public void enviarAHacienda(Comprobante comprobante) {
        Emisor emisor = comprobante.getEmisor();
        try {
            // TODO: sustituir por credenciales reales desde CredencialHacienda,
            // y por el cacheo de token (token_oauth_cache) en lugar de pedir uno nuevo cada vez.
            HaciendaAuthClient.TokenHacienda token = haciendaAuthClient.obtenerToken(
                    emisor.getAmbiente(), "usuario-atv-demo", "password-atv-demo");

            HaciendaApiClient.EnvioResultado resultado = haciendaApiClient.enviarComprobante(
                    emisor.getAmbiente(),
                    token.access_token(),
                    comprobante.getClaveNumerica(),
                    comprobante.getFechaEmision(),
                    emisor.getTipoIdentificacion(),
                    emisor.getIdentificacion(),
                    comprobante.getReceptor() != null ? comprobante.getReceptor().getTipoIdentificacion() : null,
                    comprobante.getReceptor() != null ? comprobante.getReceptor().getIdentificacion() : null,
                    comprobante.getXmlFirmado());

            if (resultado.exitoso()) {
                comprobante.setEstado(EstadoComprobante.ENVIADO);
            } else {
                comprobante.setEstado(EstadoComprobante.ERROR_ENVIO);
                comprobante.setMotivoError(resultado.detalleError());
            }
        } catch (HaciendaIntegrationException e) {
            comprobante.setEstado(EstadoComprobante.ERROR_ENVIO);
            comprobante.setMotivoError(e.getMessage());
            // TODO: encolar para reintento con backoff exponencial si e.isReintentable() es true.
        } finally {
            comprobanteRepository.save(comprobante);
        }
    }

    public ComprobanteResponse consultarEstado(String claveNumerica) {
        Comprobante comprobante = buscarPorClaveOFallar(claveNumerica);

        // TODO: usar credenciales/token reales del emisor.
        HaciendaAuthClient.TokenHacienda token = haciendaAuthClient.obtenerToken(
                comprobante.getEmisor().getAmbiente(), "usuario-atv-demo", "password-atv-demo");

        HaciendaApiClient.ConsultaResultado consulta = haciendaApiClient.consultarEstado(
                comprobante.getEmisor().getAmbiente(), token.access_token(), claveNumerica);

        actualizarEstadoDesdeHacienda(comprobante, consulta);
        return ComprobanteResponse.desde(comprobante);
    }

    @Transactional
    void actualizarEstadoDesdeHacienda(Comprobante comprobante, HaciendaApiClient.ConsultaResultado consulta) {
        switch (consulta.indEstado()) {
            case "aceptado" -> comprobante.setEstado(EstadoComprobante.ACEPTADO);
            case "rechazado" -> comprobante.setEstado(EstadoComprobante.RECHAZADO);
            default -> { /* se mantiene el estado actual mientras Hacienda procesa */ }
        }
        comprobanteRepository.save(comprobante);
    }

    public ComprobanteResponse obtenerPorClave(String claveNumerica) {
        return ComprobanteResponse.desde(buscarPorClaveOFallar(claveNumerica));
    }

    public String obtenerXml(String claveNumerica) {
        Comprobante c = buscarPorClaveOFallar(claveNumerica);
        if (c.getXmlFirmado() == null) {
            throw new BusinessException("El comprobante aún no ha sido firmado");
        }
        return c.getXmlFirmado();
    }

    public Page<ComprobanteResponse> listar(UUID emisorId, EstadoComprobante estado, Pageable pageable) {
        Page<Comprobante> pagina = estado == null
                ? comprobanteRepository.findByEmisorId(emisorId, pageable)
                : comprobanteRepository.findByEmisorIdAndEstado(emisorId, estado, pageable);
        return pagina.map(ComprobanteResponse::desde);
    }

    @Transactional
    public ComprobanteResponse reenviar(String claveNumerica) {
        Comprobante comprobante = buscarPorClaveOFallar(claveNumerica);
        if (comprobante.getEstado() != EstadoComprobante.ERROR_ENVIO) {
            throw new BusinessException("Solo se pueden reenviar comprobantes en estado ERROR_ENVIO");
        }
        enviarAHacienda(comprobante);
        return ComprobanteResponse.desde(comprobante);
    }

    // ---------------------------------------------------------------------
    // Métodos privados de apoyo
    // ---------------------------------------------------------------------

    private Receptor resolverReceptor(Emisor emisor, ComprobanteRequest.ReceptorInline datos) {
        if (datos.id() != null) {
            return receptorRepository.findById(datos.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Receptor no encontrado: " + datos.id()));
        }
        // Crea (o reutiliza) el receptor a partir de los datos inline del request.
        return receptorRepository.findByEmisorIdAndIdentificacion(emisor.getId(), datos.identificacion())
                .orElseGet(() -> receptorRepository.save(Receptor.builder()
                        .emisor(emisor)
                        .tipoIdentificacion(datos.tipoIdentificacion())
                        .identificacion(datos.identificacion())
                        .nombre(datos.nombre())
                        .correo(datos.correo())
                        .codigoActividadReceptor(datos.codigoActividadReceptor())
                        .build()));
    }

    private void agregarLineasDetalle(Comprobante comprobante, ComprobanteRequest request) {
        for (ComprobanteRequest.DetalleLinea l : request.detalle()) {
            BigDecimal subtotal = l.precioUnitario().multiply(l.cantidad());
            BigDecimal descuento = l.montoDescuento() != null ? l.montoDescuento() : BigDecimal.ZERO;
            BigDecimal montoImpuestos = l.impuestos().stream()
                    .map(ComprobanteRequest.ImpuestoLinea::monto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalLinea = subtotal.subtract(descuento).add(montoImpuestos);

            ComprobanteDetalle detalle = ComprobanteDetalle.builder()
                    .numeroLinea(l.numeroLinea())
                    .codigoCabys(l.codigoCabys())
                    .cantidad(l.cantidad())
                    .unidadMedida(l.unidadMedida())
                    .detalle(l.detalle())
                    .precioUnitario(l.precioUnitario())
                    .montoDescuento(descuento)
                    .naturalezaDescuento(l.naturalezaDescuento())
                    .subtotal(subtotal)
                    .montoTotalLinea(totalLinea)
                    .tipoTransaccion(l.tipoTransaccion())
                    .build();

            for (ComprobanteRequest.ImpuestoLinea imp : l.impuestos()) {
                ComprobanteImpuesto impuesto = ComprobanteImpuesto.builder()
                        .codigoImpuesto(imp.codigo())
                        .codigoTarifaIva(imp.codigoTarifaIva())
                        .tarifa(imp.tarifa())
                        .monto(imp.monto())
                        .build();
                detalle.agregarImpuesto(impuesto);
            }
            comprobante.agregarDetalle(detalle);
        }
    }

    private void calcularTotales(Comprobante comprobante) {
        BigDecimal totalGravado = BigDecimal.ZERO;
        BigDecimal totalImpuesto = BigDecimal.ZERO;
        BigDecimal totalDescuento = BigDecimal.ZERO;

        for (ComprobanteDetalle linea : comprobante.getDetalle()) {
            totalGravado = totalGravado.add(linea.getSubtotal());
            totalDescuento = totalDescuento.add(linea.getMontoDescuento());
            for (ComprobanteImpuesto imp : linea.getImpuestos()) {
                totalImpuesto = totalImpuesto.add(imp.getMonto());
            }
        }

        BigDecimal totalComprobante = totalGravado.subtract(totalDescuento).add(totalImpuesto)
                .setScale(5, RoundingMode.HALF_UP);

        comprobante.setTotalGravado(totalGravado.setScale(5, RoundingMode.HALF_UP));
        comprobante.setTotalImpuesto(totalImpuesto.setScale(5, RoundingMode.HALF_UP));
        comprobante.setTotalDescuento(totalDescuento.setScale(5, RoundingMode.HALF_UP));
        comprobante.setTotalComprobante(totalComprobante);
    }

    private Comprobante buscarPorClaveOFallar(String claveNumerica) {
        return comprobanteRepository.findByClaveNumerica(claveNumerica)
                .orElseThrow(() -> new ResourceNotFoundException("Comprobante no encontrado: " + claveNumerica));
    }

    // Placeholders — sustituir por lectura real y desencriptado de CertificadoCriptografico.
    private byte[] obtenerCertificadoDelEmisor(Emisor emisor) {
        throw new IllegalStateException("Certificado criptográfico no configurado para el emisor " + emisor.getId());
    }

    private char[] obtenerPasswordDelEmisor(Emisor emisor) {
        throw new IllegalStateException("Password de certificado no configurada para el emisor " + emisor.getId());
    }
}
