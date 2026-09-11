package com.factuelectronica.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "comprobante_impuesto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComprobanteImpuesto {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "detalle_id", nullable = false)
    private ComprobanteDetalle detalle;

    @Column(name = "codigo_impuesto", nullable = false, length = 2)
    @Builder.Default
    private String codigoImpuesto = "01"; // 01 = IVA

    @Column(name = "codigo_tarifa_iva", length = 2)
    private String codigoTarifaIva;

    @Column(name = "tarifa", nullable = false, precision = 5, scale = 2)
    private BigDecimal tarifa;

    @Column(name = "monto", nullable = false, precision = 18, scale = 5)
    private BigDecimal monto;

    // ---- Exoneración (opcional) ----
    @Column(name = "exoneracion_tipo_doc", length = 2)
    private String exoneracionTipoDoc;

    @Column(name = "exoneracion_numero_doc", length = 40)
    private String exoneracionNumeroDoc;

    @Column(name = "exoneracion_institucion", length = 50)
    private String exoneracionInstitucion;

    @Column(name = "exoneracion_fecha")
    private LocalDate exoneracionFecha;

    @Column(name = "exoneracion_porcentaje", precision = 5, scale = 2)
    private BigDecimal exoneracionPorcentaje;

    @Column(name = "exoneracion_monto", precision = 18, scale = 5)
    private BigDecimal exoneracionMonto;
}
