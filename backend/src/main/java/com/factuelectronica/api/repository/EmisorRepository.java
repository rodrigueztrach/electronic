package com.factuelectronica.api.repository;

import com.factuelectronica.api.model.Emisor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmisorRepository extends JpaRepository<Emisor, UUID> {
    Optional<Emisor> findByIdentificacion(String identificacion);
    boolean existsByIdentificacion(String identificacion);
}
