package com.factuelectronica.api.repository;

import com.factuelectronica.api.model.Comprobante;
import com.factuelectronica.api.model.EstadoComprobante;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ComprobanteRepository extends JpaRepository<Comprobante, UUID> {

    Optional<Comprobante> findByClaveNumerica(String claveNumerica);

    boolean existsByClaveNumerica(String claveNumerica);

    Page<Comprobante> findByEmisorId(UUID emisorId, Pageable pageable);

    Page<Comprobante> findByEmisorIdAndEstado(UUID emisorId, EstadoComprobante estado, Pageable pageable);

    List<Comprobante> findByEstadoInOrderByCreadoEnAsc(List<EstadoComprobante> estados);
}
