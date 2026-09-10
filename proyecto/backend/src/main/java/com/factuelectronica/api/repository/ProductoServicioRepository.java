package com.factuelectronica.api.repository;

import com.factuelectronica.api.model.ProductoServicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductoServicioRepository extends JpaRepository<ProductoServicio, UUID> {
    Page<ProductoServicio> findByEmisorId(UUID emisorId, Pageable pageable);
}
