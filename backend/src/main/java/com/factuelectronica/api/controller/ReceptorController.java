package com.factuelectronica.api.controller;

import com.factuelectronica.api.dto.ReceptorRequest;
import com.factuelectronica.api.dto.ReceptorResponse;
import com.factuelectronica.api.service.ReceptorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/receptores")
@RequiredArgsConstructor
@Tag(name = "Receptores", description = "Gestión de clientes/receptores de comprobantes")
public class ReceptorController {

    private final ReceptorService receptorService;

    @PostMapping
    @Operation(summary = "Crea un nuevo receptor")
    public ResponseEntity<ReceptorResponse> crear(@Valid @RequestBody ReceptorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(receptorService.crear(request));
    }

    @GetMapping
    @Operation(summary = "Lista receptores de un emisor, con búsqueda opcional por nombre")
    public Page<ReceptorResponse> listar(@RequestParam UUID emisorId,
                                          @RequestParam(required = false) String nombre,
                                          Pageable pageable) {
        return receptorService.listar(emisorId, nombre, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene el detalle de un receptor")
    public ReceptorResponse obtener(@PathVariable UUID id) {
        return receptorService.obtener(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza los datos de un receptor")
    public ReceptorResponse actualizar(@PathVariable UUID id, @Valid @RequestBody ReceptorRequest request) {
        return receptorService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Inactiva (borrado lógico) un receptor")
    public ResponseEntity<Void> inactivar(@PathVariable UUID id) {
        receptorService.inactivar(id);
        return ResponseEntity.noContent().build();
    }
}
