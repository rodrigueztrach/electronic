package com.factuelectronica.api.repository;

import com.factuelectronica.api.model.Receptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReceptorRepository extends JpaRepository<Receptor, UUID> {

    Page<Receptor> findByEmisorId(UUID emisorId, Pageable pageable);

    Optional<Receptor> findByEmisorIdAndIdentificacion(UUID emisorId, String identificacion);

    Page<Receptor> findByEmisorIdAndNombreContainingIgnoreCase(UUID emisorId, String nombre, Pageable pageable);
}
