package com.factuelectronica.api.service;

import com.factuelectronica.api.dto.ProductoRequest;
import com.factuelectronica.api.dto.ProductoResponse;
import com.factuelectronica.api.exception.ResourceNotFoundException;
import com.factuelectronica.api.model.Emisor;
import com.factuelectronica.api.model.ProductoServicio;
import com.factuelectronica.api.repository.ProductoServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoServicioRepository productoRepository;
    private final EmisorService emisorService;

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        Emisor emisor = emisorService.buscarOFallar(request.emisorId());
        ProductoServicio producto = ProductoServicio.builder()
                .emisor(emisor)
                .codigoInterno(request.codigoInterno())
                .codigoCabys(request.codigoCabys())
                .nombre(request.nombre())
                .unidadMedida(request.unidadMedida())
                .precioUnitario(request.precioUnitario())
                .tarifaImpuesto(request.tarifaImpuesto() != null ? request.tarifaImpuesto() : new BigDecimal("13.00"))
                .registroMedicamento(request.registroMedicamento())
                .formaFarmaceutica(request.formaFarmaceutica())
                .build();
        return ProductoResponse.desde(productoRepository.save(producto));
    }

    public Page<ProductoResponse> listar(UUID emisorId, Pageable pageable) {
        return productoRepository.findByEmisorId(emisorId, pageable).map(ProductoResponse::desde);
    }

    @Transactional
    public ProductoResponse actualizar(UUID id, ProductoRequest request) {
        ProductoServicio producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + id));
        producto.setCodigoInterno(request.codigoInterno());
        producto.setCodigoCabys(request.codigoCabys());
        producto.setNombre(request.nombre());
        producto.setUnidadMedida(request.unidadMedida());
        producto.setPrecioUnitario(request.precioUnitario());
        if (request.tarifaImpuesto() != null) {
            producto.setTarifaImpuesto(request.tarifaImpuesto());
        }
        producto.setRegistroMedicamento(request.registroMedicamento());
        producto.setFormaFarmaceutica(request.formaFarmaceutica());
        return ProductoResponse.desde(productoRepository.save(producto));
    }
}
