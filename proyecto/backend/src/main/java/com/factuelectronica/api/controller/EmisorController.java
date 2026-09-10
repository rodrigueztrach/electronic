package com.factuelectronica.api.controller;

import com.factuelectronica.api.dto.EmisorRequest;
import com.factuelectronica.api.dto.EmisorResponse;
import com.factuelectronica.api.service.EmisorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/emisores")
@RequiredArgsConstructor
@Tag(name = "Emisores", description = "Gestión de contribuyentes emisores de comprobantes")
public class EmisorController {

    private final EmisorService emisorService;

    @PostMapping
    @Operation(summary = "Registra un nuevo emisor")
    public ResponseEntity<EmisorResponse> crear(@Valid @RequestBody EmisorRequest request) {
        EmisorResponse creado = emisorService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping
    @Operation(summary = "Lista todos los emisores")
    public List<EmisorResponse> listar() {
        return emisorService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene el detalle de un emisor")
    public EmisorResponse obtener(@PathVariable UUID id) {
        return emisorService.obtener(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza los datos de un emisor")
    public EmisorResponse actualizar(@PathVariable UUID id, @Valid @RequestBody EmisorRequest request) {
        return emisorService.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Activa o inactiva un emisor")
    public ResponseEntity<Void> cambiarEstado(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        emisorService.cambiarEstado(id, body.getOrDefault("estado", "activo"));
        return ResponseEntity.noContent().build();
    }
}
