package com.factuelectronica.api.controller;

import com.factuelectronica.api.dto.ComprobanteRequest;
import com.factuelectronica.api.dto.ComprobanteResponse;
import com.factuelectronica.api.model.EstadoComprobante;
import com.factuelectronica.api.service.ComprobanteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/comprobantes")
@RequiredArgsConstructor
@Tag(name = "Comprobantes", description = "Emisión, consulta y gestión de comprobantes electrónicos v4.4")
public class ComprobanteController {

    private final ComprobanteService comprobanteService;

    @PostMapping
    @Operation(summary = "Crea y encola la emisión de un comprobante (FE, TE, NC, ND, FEC, FEE, REP)")
    public ResponseEntity<ComprobanteResponse> crear(
            @Valid @RequestBody ComprobanteRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        // TODO: usar idempotencyKey para deduplicar solicitudes repetidas del mismo cliente
        // (por ejemplo, cacheando el resultado en Redis durante 24 horas).
        ComprobanteResponse creado = comprobanteService.crearYEmitir(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(creado);
    }

    @GetMapping
    @Operation(summary = "Lista comprobantes de un emisor, con filtro opcional por estado")
    public Page<ComprobanteResponse> listar(@RequestParam UUID emisorId,
                                             @RequestParam(required = false) EstadoComprobante estado,
                                             Pageable pageable) {
        return comprobanteService.listar(emisorId, estado, pageable);
    }

    @GetMapping("/{claveNumerica}")
    @Operation(summary = "Obtiene el detalle de un comprobante por su clave numérica")
    public ComprobanteResponse obtener(@PathVariable String claveNumerica) {
        return comprobanteService.obtenerPorClave(claveNumerica);
    }

    @GetMapping(value = "/{claveNumerica}/xml", produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(summary = "Descarga el XML firmado del comprobante")
    public ResponseEntity<String> obtenerXml(@PathVariable String claveNumerica) {
        String xml = comprobanteService.obtenerXml(claveNumerica);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + claveNumerica + ".xml\"")
                .body(xml);
    }

    @GetMapping("/{claveNumerica}/estado")
    @Operation(summary = "Fuerza una re-consulta del estado contra Hacienda")
    public ComprobanteResponse consultarEstado(@PathVariable String claveNumerica) {
        return comprobanteService.consultarEstado(claveNumerica);
    }

    @PostMapping("/{claveNumerica}/reenviar")
    @Operation(summary = "Reintenta el envío de un comprobante en estado de error")
    public ComprobanteResponse reenviar(@PathVariable String claveNumerica) {
        return comprobanteService.reenviar(claveNumerica);
    }
}
