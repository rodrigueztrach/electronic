package com.factuelectronica.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "comprobante_detalle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComprobanteDetalle {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprobante_id", nullable = false)
    private Comprobante comprobante;

    /**
     * Copia de comprobante.fechaEmision, requerida por la llave foránea compuesta
     * (comprobante_id, comprobante_fecha) hacia la tabla particionada "comprobante".
     * Se sincroniza automáticamente en @PrePersist.
     */
    @Column(name = "comprobante_fecha", nullable = false)
    private OffsetDateTime comprobanteFecha;

    @Column(name = "numero_linea", nullable = false)
    private Integer numeroLinea;

    @Column(name = "codigo_cabys", nullable = false, length = 13)
    private String codigoCabys;

    @Column(name = "codigo_comercial")
    private String codigoComercial;

    @Column(name = "cantidad", nullable = false, precision = 18, scale = 5)
    @Builder.Default
    private BigDecimal cantidad = BigDecimal.ONE;

    @Column(name = "unidad_medida", nullable = false, length = 10)
    @Builder.Default
    private String unidadMedida = "Unid";

    @Column(name = "detalle", nullable = false)
    private String detalle;

    @Column(name = "precio_unitario", nullable = false, precision = 18, scale = 5)
    private BigDecimal precioUnitario;

    @Column(name = "monto_descuento", nullable = false, precision = 18, scale = 5)
    @Builder.Default
    private BigDecimal montoDescuento = BigDecimal.ZERO;

    @Column(name = "naturaleza_descuento")
    private String naturalezaDescuento;

    @Column(name = "subtotal", nullable = false, precision = 18, scale = 5)
    private BigDecimal subtotal;

    @Column(name = "monto_total_linea", nullable = false, precision = 18, scale = 5)
    private BigDecimal montoTotalLinea;

    @Column(name = "tipo_transaccion", length = 2)
    private String tipoTransaccion;

    @OneToMany(mappedBy = "detalle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ComprobanteImpuesto> impuestos = new ArrayList<>();

    @PrePersist
    void sincronizarFechaComprobante() {
        if (comprobante != null) {
            this.comprobanteFecha = comprobante.getFechaEmision();
        }
    }

    public void agregarImpuesto(ComprobanteImpuesto impuesto) {
        impuesto.setDetalle(this);
        this.impuestos.add(impuesto);
    }
}
