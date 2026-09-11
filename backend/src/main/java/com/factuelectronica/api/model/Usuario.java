package com.factuelectronica.api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "correo", nullable = false, unique = true, length = 150)
    private String correo;

    @Column(name = "nombre_completo", nullable = false, length = 200)
    private String nombreCompleto;

    /** Hash BCrypt de la contraseña — nunca se almacena en texto plano. */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "estado", nullable = false, length = 15)
    @Builder.Default
    private String estado = "activo";

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private OffsetDateTime creadoEn;
}