package com.factuelectronica.api.service;

import com.factuelectronica.api.dto.EmisorRequest;
import com.factuelectronica.api.dto.EmisorResponse;
import com.factuelectronica.api.exception.BusinessException;
import com.factuelectronica.api.exception.ResourceNotFoundException;
import com.factuelectronica.api.model.Ambiente;
import com.factuelectronica.api.model.Emisor;
import com.factuelectronica.api.repository.EmisorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmisorService {

    private final EmisorRepository emisorRepository;

    @Transactional
    public EmisorResponse crear(EmisorRequest request) {
        if (emisorRepository.existsByIdentificacion(request.identificacion())) {
            throw new BusinessException("Ya existe un emisor registrado con esa identificación");
        }
        Emisor emisor = Emisor.builder()
                .tipoIdentificacion(request.tipoIdentificacion())
                .identificacion(request.identificacion())
                .razonSocial(request.razonSocial())
                .nombreComercial(request.nombreComercial())
                .actividadEconomica(request.actividadEconomica())
                .correoNotificacion(request.correoNotificacion())
                .ambiente("produccion".equals(request.ambiente()) ? Ambiente.PRODUCCION : Ambiente.SANDBOX)
                .proveedorSistemas(request.proveedorSistemas())
                .build();
        return EmisorResponse.desde(emisorRepository.save(emisor));
    }

    public EmisorResponse obtener(UUID id) {
        return EmisorResponse.desde(buscarOFallar(id));
    }

    public List<EmisorResponse> listar() {
        return emisorRepository.findAll().stream().map(EmisorResponse::desde).toList();
    }

    @Transactional
    public EmisorResponse actualizar(UUID id, EmisorRequest request) {
        Emisor emisor = buscarOFallar(id);
        emisor.setRazonSocial(request.razonSocial());
        emisor.setNombreComercial(request.nombreComercial());
        emisor.setActividadEconomica(request.actividadEconomica());
        emisor.setCorreoNotificacion(request.correoNotificacion());
        emisor.setProveedorSistemas(request.proveedorSistemas());
        return EmisorResponse.desde(emisorRepository.save(emisor));
    }

    @Transactional
    public void cambiarEstado(UUID id, String nuevoEstado) {
        Emisor emisor = buscarOFallar(id);
        emisor.setEstado(nuevoEstado);
        emisorRepository.save(emisor);
    }

    Emisor buscarOFallar(UUID id) {
        return emisorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emisor no encontrado: " + id));
    }
}
