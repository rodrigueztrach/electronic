package com.factuelectronica.api.controller;

import com.factuelectronica.api.dto.ProductoRequest;
import com.factuelectronica.api.dto.ProductoResponse;
import com.factuelectronica.api.service.ProductoService;
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
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Catálogo interno de productos y servicios")
public class ProductoController {

    private final ProductoService productoService;

    @PostMapping
    @Operation(summary = "Crea un producto/servicio en el catálogo interno")
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(request));
    }

    @GetMapping
    @Operation(summary = "Lista productos de un emisor")
    public Page<ProductoResponse> listar(@RequestParam UUID emisorId, Pageable pageable) {
        return productoService.listar(emisorId, pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza un producto")
    public ProductoResponse actualizar(@PathVariable UUID id, @Valid @RequestBody ProductoRequest request) {
        return productoService.actualizar(id, request);
    }
}
