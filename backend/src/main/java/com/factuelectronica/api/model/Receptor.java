package com.factuelectronica.api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "receptor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Receptor {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emisor_id", nullable = false)
    private Emisor emisor;

    @Column(name = "tipo_identificacion", nullable = false, length = 2)
    private String tipoIdentificacion;

    @Column(name = "identificacion", nullable = false, length = 20)
    private String identificacion;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "correo")
    private String correo;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "codigo_actividad_receptor", length = 10)
    private String codigoActividadReceptor;

    @Column(name = "provincia", length = 2)
    private String provincia;

    @Column(name = "canton", length = 2)
    private String canton;

    @Column(name = "distrito", length = 2)
    private String distrito;

    @Column(name = "otras_senas")
    private String otrasSenas;

    @Column(name = "estado", nullable = false, length = 15)
    @Builder.Default
    private String estado = "activo";

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private OffsetDateTime creadoEn;
}
