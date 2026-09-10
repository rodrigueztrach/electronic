package com.factuelectronica.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ComprobanteRequest(

        @NotNull UUID emisorId,

        @NotBlank @Pattern(regexp = "01|02|03|04|08|09|10")
        String tipoDocumento,

        @NotBlank @Pattern(regexp = "0[1-4]")
        String condicionVenta,

        String plazoCredito,

        List<String> medioPago,

        @NotBlank @Pattern(regexp = "[A-Z]{3}")
        String moneda,

        BigDecimal tipoCambio,

        @Valid @NotNull
        ReceptorInline receptor,

        @Valid @NotEmpty
        List<DetalleLinea> detalle,

        /** Requerido solo para NC / ND / REP: referencia al comprobante original */
        @Valid
        List<ReferenciaDocumento> referencias,

        String proveedorSistemas
) {

    public record ReceptorInline(
            UUID id, // si se referencia un receptor ya existente
            String tipoIdentificacion,
            String identificacion,
            @NotBlank String nombre,
            @Email String correo,
            String codigoActividadReceptor
    ) {}

    public record DetalleLinea(
            @NotNull @Min(1) Integer numeroLinea,
            @NotBlank @Pattern(regexp = "\\d{13}") String codigoCabys,
            @NotNull @DecimalMin("0.00001") BigDecimal cantidad,
            @NotBlank String unidadMedida,
            @NotBlank String detalle,
            @NotNull @DecimalMin("0") BigDecimal precioUnitario,
            BigDecimal montoDescuento,
            String naturalezaDescuento,
            String tipoTransaccion,
            @Valid @NotEmpty List<ImpuestoLinea> impuestos
    ) {}

    public record ImpuestoLinea(
            @NotBlank String codigo,
            String codigoTarifaIva,
            @NotNull @DecimalMin("0") BigDecimal tarifa,
            @NotNull @DecimalMin("0") BigDecimal monto,
            ExoneracionLinea exoneracion
    ) {}

    public record ExoneracionLinea(
            String tipoDocumento,
            String numeroDocumento,
            String institucion,
            String fecha,           // ISO-8601 (yyyy-MM-dd)
            BigDecimal porcentaje,
            BigDecimal monto
    ) {}

    public record ReferenciaDocumento(
            @NotBlank String tipoDocumento,
            @NotBlank String numeroDocumento,
            @NotBlank String fechaEmision,
            @NotBlank String codigoRazon,
            String razon
    ) {}
}
