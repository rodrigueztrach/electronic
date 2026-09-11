package com.factuelectronica.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "emisor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Emisor {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "tipo_identificacion", nullable = false, length = 2)
    private String tipoIdentificacion;

    @Column(name = "identificacion", nullable = false, unique = true, length = 20)
    private String identificacion;

    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    @Column(name = "nombre_comercial")
    private String nombreComercial;

    @Column(name = "actividad_economica", nullable = false, length = 10)
    private String actividadEconomica;

    @Column(name = "correo_notificacion")
    private String correoNotificacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "ambiente", nullable = false, length = 10)
    @Builder.Default
    private Ambiente ambiente = Ambiente.SANDBOX;

    @Column(name = "version_esquema", nullable = false, length = 5)
    @Builder.Default
    private String versionEsquema = "4.4";

    @Column(name = "proveedor_sistemas", length = 20)
    private String proveedorSistemas;

    @Column(name = "estado", nullable = false, length = 15)
    @Builder.Default
    private String estado = "activo";

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private OffsetDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private OffsetDateTime actualizadoEn;
}
