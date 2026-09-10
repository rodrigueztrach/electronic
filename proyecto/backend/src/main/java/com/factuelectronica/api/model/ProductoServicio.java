package com.factuelectronica.api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "producto_servicio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoServicio {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emisor_id", nullable = false)
    private Emisor emisor;

    @Column(name = "codigo_interno")
    private String codigoInterno;

    @Column(name = "codigo_cabys", nullable = false, length = 13)
    private String codigoCabys;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "unidad_medida", nullable = false, length = 10)
    @Builder.Default
    private String unidadMedida = "Unid";

    @Column(name = "precio_unitario", nullable = false, precision = 18, scale = 5)
    private BigDecimal precioUnitario;

    @Column(name = "tarifa_impuesto", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal tarifaImpuesto = new BigDecimal("13.00");

    @Column(name = "registro_medicamento")
    private String registroMedicamento;

    @Column(name = "forma_farmaceutica")
    private String formaFarmaceutica;

    @Column(name = "estado", nullable = false, length = 15)
    @Builder.Default
    private String estado = "activo";

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private OffsetDateTime creadoEn;
}
