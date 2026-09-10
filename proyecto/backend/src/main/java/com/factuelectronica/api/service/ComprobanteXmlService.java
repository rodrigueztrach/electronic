package com.factuelectronica.api.service;

import com.factuelectronica.api.model.Comprobante;
import com.factuelectronica.api.model.ComprobanteDetalle;
import com.factuelectronica.api.model.ComprobanteImpuesto;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Construye el XML del comprobante conforme a la estructura v4.4.
 *
 * NOTA IMPORTANTE: esta implementación genera una representación simplificada
 * con fines demostrativos. En producción el XML debe:
 *   1) Seguir exactamente el namespace y los elementos del XSD oficial
 *      correspondiente al tipo de documento (FacturaElectronica, TiqueteElectronico,
 *      NotaCreditoElectronica, NotaDebitoElectronica, FacturaElectronicaCompra,
 *      FacturaElectronicaExportacion, ReciboElectronicoPago).
 *   2) Validarse contra el XSD oficial de la versión 4.4 antes de firmarse
 *      (ver XsdValidationService, no incluido en este starter).
 *   3) Usar una librería de serialización XML robusta (JAXB) en lugar de
 *      concatenación de cadenas.
 */
@Service
public class ComprobanteXmlService {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public String construirXml(Comprobante comprobante) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append(rootElementFor(comprobante)).append("\n");
        xml.append("  <Clave>").append(comprobante.getClaveNumerica()).append("</Clave>\n");
        xml.append("  <ProveedorSistemas>").append(nvl(comprobante.getProveedorSistemas())).append("</ProveedorSistemas>\n");
        xml.append("  <CodigoActividadEmisor>").append(comprobante.getEmisor().getActividadEconomica()).append("</CodigoActividadEmisor>\n");
        xml.append("  <NumeroConsecutivo>").append(comprobante.getConsecutivo()).append("</NumeroConsecutivo>\n");
        xml.append("  <FechaEmision>").append(comprobante.getFechaEmision().format(ISO)).append("</FechaEmision>\n");
        xml.append("  <Emisor>\n");
        xml.append("    <Identificacion>\n");
        xml.append("      <Tipo>").append(comprobante.getEmisor().getTipoIdentificacion()).append("</Tipo>\n");
        xml.append("      <Numero>").append(comprobante.getEmisor().getIdentificacion()).append("</Numero>\n");
        xml.append("    </Identificacion>\n");
        xml.append("    <Nombre>").append(escapar(comprobante.getEmisor().getRazonSocial())).append("</Nombre>\n");
        xml.append("  </Emisor>\n");

        if (comprobante.getReceptor() != null) {
            xml.append("  <Receptor>\n");
            xml.append("    <Identificacion>\n");
            xml.append("      <Tipo>").append(comprobante.getReceptor().getTipoIdentificacion()).append("</Tipo>\n");
            xml.append("      <Numero>").append(comprobante.getReceptor().getIdentificacion()).append("</Numero>\n");
            xml.append("    </Identificacion>\n");
            xml.append("    <Nombre>").append(escapar(comprobante.getReceptor().getNombre())).append("</Nombre>\n");
            if (comprobante.getReceptor().getCodigoActividadReceptor() != null) {
                xml.append("    <CodigoActividadReceptor>")
                        .append(comprobante.getReceptor().getCodigoActividadReceptor())
                        .append("</CodigoActividadReceptor>\n");
            }
            xml.append("  </Receptor>\n");
        }

        xml.append("  <CondicionVenta>").append(comprobante.getCondicionVenta()).append("</CondicionVenta>\n");
        xml.append("  <DetalleServicio>\n");
        for (ComprobanteDetalle linea : comprobante.getDetalle()) {
            xml.append("    <LineaDetalle>\n");
            xml.append("      <NumeroLinea>").append(linea.getNumeroLinea()).append("</NumeroLinea>\n");
            xml.append("      <Codigo>").append(linea.getCodigoCabys()).append("</Codigo>\n");
            xml.append("      <Cantidad>").append(linea.getCantidad()).append("</Cantidad>\n");
            xml.append("      <UnidadMedida>").append(linea.getUnidadMedida()).append("</UnidadMedida>\n");
            xml.append("      <Detalle>").append(escapar(linea.getDetalle())).append("</Detalle>\n");
            xml.append("      <PrecioUnitario>").append(linea.getPrecioUnitario()).append("</PrecioUnitario>\n");
            xml.append("      <MontoTotalLinea>").append(linea.getMontoTotalLinea()).append("</MontoTotalLinea>\n");
            for (ComprobanteImpuesto impuesto : linea.getImpuestos()) {
                xml.append("      <Impuesto>\n");
                xml.append("        <Codigo>").append(impuesto.getCodigoImpuesto()).append("</Codigo>\n");
                xml.append("        <Tarifa>").append(impuesto.getTarifa()).append("</Tarifa>\n");
                xml.append("        <Monto>").append(impuesto.getMonto()).append("</Monto>\n");
                xml.append("      </Impuesto>\n");
            }
            xml.append("    </LineaDetalle>\n");
        }
        xml.append("  </DetalleServicio>\n");

        xml.append("  <ResumenFactura>\n");
        xml.append("    <TotalGravado>").append(comprobante.getTotalGravado()).append("</TotalGravado>\n");
        xml.append("    <TotalExento>").append(comprobante.getTotalExento()).append("</TotalExento>\n");
        xml.append("    <TotalImpuesto>").append(comprobante.getTotalImpuesto()).append("</TotalImpuesto>\n");
        xml.append("    <TotalComprobante>").append(comprobante.getTotalComprobante()).append("</TotalComprobante>\n");
        xml.append("  </ResumenFactura>\n");
        xml.append(closingElementFor(comprobante)).append("\n");
        return xml.toString();
    }

    private String rootElementFor(Comprobante c) {
        return switch (c.getTipoDocumento()) {
            case FACTURA_ELECTRONICA -> "<FacturaElectronica xmlns=\"https://cdn.comprobanteselectronicos.go.cr/xml-schemas/v4.4/facturaElectronica\">";
            case TIQUETE_ELECTRONICO -> "<TiqueteElectronico xmlns=\"https://cdn.comprobanteselectronicos.go.cr/xml-schemas/v4.4/tiqueteElectronico\">";
            case NOTA_CREDITO -> "<NotaCreditoElectronica xmlns=\"https://cdn.comprobanteselectronicos.go.cr/xml-schemas/v4.4/notaCreditoElectronica\">";
            case NOTA_DEBITO -> "<NotaDebitoElectronica xmlns=\"https://cdn.comprobanteselectronicos.go.cr/xml-schemas/v4.4/notaDebitoElectronica\">";
            case FACTURA_ELECTRONICA_COMPRA -> "<FacturaElectronicaCompra xmlns=\"https://cdn.comprobanteselectronicos.go.cr/xml-schemas/v4.4/facturaElectronicaCompra\">";
            case FACTURA_ELECTRONICA_EXPORTACION -> "<FacturaElectronicaExportacion xmlns=\"https://cdn.comprobanteselectronicos.go.cr/xml-schemas/v4.4/facturaElectronicaExportacion\">";
            case RECIBO_ELECTRONICO_PAGO -> "<ReciboElectronicoPago xmlns=\"https://cdn.comprobanteselectronicos.go.cr/xml-schemas/v4.4/reciboElectronicoPago\">";
        };
    }

    private String closingElementFor(Comprobante c) {
        return switch (c.getTipoDocumento()) {
            case FACTURA_ELECTRONICA -> "</FacturaElectronica>";
            case TIQUETE_ELECTRONICO -> "</TiqueteElectronico>";
            case NOTA_CREDITO -> "</NotaCreditoElectronica>";
            case NOTA_DEBITO -> "</NotaDebitoElectronica>";
            case FACTURA_ELECTRONICA_COMPRA -> "</FacturaElectronicaCompra>";
            case FACTURA_ELECTRONICA_EXPORTACION -> "</FacturaElectronicaExportacion>";
            case RECIBO_ELECTRONICO_PAGO -> "</ReciboElectronicoPago>";
        };
    }

    private String escapar(String texto) {
        if (texto == null) return "";
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String nvl(String valor) {
        return valor == null ? "" : valor;
    }
}
