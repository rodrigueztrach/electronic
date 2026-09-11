package com.factuelectronica.api.service;

/**
 * Contrato para la firma digital de comprobantes electrónicos con el
 * estándar XAdES-EPES exigido por el Ministerio de Hacienda.
 *
 * La implementación real debe:
 *  - Cargar el certificado .p12 del emisor (desencriptado en memoria, nunca en disco).
 *  - Firmar usando Apache Santuario (xmlsec) o una librería equivalente que
 *    soporte el perfil XAdES-EPES con política de firma explícita.
 *  - Incrustar la firma dentro del elemento raíz del comprobante, según
 *    la posición esperada por el XSD de Hacienda.
 */
public interface XmlSignerService {

    /**
     * Firma el XML dado y devuelve el XML firmado como cadena.
     *
     * @param xmlSinFirmar      XML del comprobante generado por ComprobanteXmlService
     * @param certificadoP12    bytes del certificado .p12 (ya desencriptado)
     * @param passwordP12       contraseña del certificado .p12 (ya desencriptada)
     * @return XML firmado (XAdES-EPES) listo para transmitir a Hacienda
     */
    String firmar(String xmlSinFirmar, byte[] certificadoP12, char[] passwordP12);
}
