package com.factuelectronica.api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "mensaje_hacienda")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeHacienda {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprobante_id", nullable = false)
    private Comprobante comprobante;

    @Column(name = "comprobante_fecha", nullable = false)
    private OffsetDateTime comprobanteFecha;

    /** recibido | procesando | aceptado | rechazado | error */
    @Column(name = "ind_estado", nullable = false, length = 20)
    private String indEstado;

    @Lob
    @Column(name = "xml_respuesta", columnDefinition = "TEXT")
    private String xmlRespuesta;

    @Column(name = "detalle_error", length = 1000)
    private String detalleError;

    @CreationTimestamp
    @Column(name = "fecha_respuesta")
    private OffsetDateTime fechaRespuesta;

    @PrePersist
    void sincronizarFechaComprobante() {
        if (comprobante != null) {
            this.comprobanteFecha = comprobante.getFechaEmision();
        }
    }
}
