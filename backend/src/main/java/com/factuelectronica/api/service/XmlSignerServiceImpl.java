package com.factuelectronica.api.service;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

/**
 * Implementación de referencia usando la API estándar de Java (javax.xml.crypto.dsig)
 * para producir una firma XML enveloped (XML-DSig) sobre el comprobante.
 *
 * IMPORTANTE — brecha de cumplimiento a cerrar antes de producción:
 * El Ministerio de Hacienda exige el perfil XAdES-EPES, que añade sobre XML-DSig:
 *   - Elemento <xades:QualifyingProperties> con <SignedProperties>
 *     (SigningTime, SigningCertificate, SignaturePolicyIdentifier).
 *   - Referencia adicional a esas SignedProperties dentro de la firma.
 * Para XAdES-EPES completo se recomienda usar una librería especializada
 * (p. ej. Apache Santuario + xades4j, o una librería .NET/Java ya probada
 * contra el validador de Hacienda) en lugar de construir el perfil a mano.
 * Esta clase deja la base de firma XML-DSig correctamente implementada para
 * que dicha extensión XAdES se añada como un paso adicional sobre el mismo
 * DOMSignContext.
 */
@Service
public class XmlSignerServiceImpl implements XmlSignerService {

    @Override
    public String firmar(String xmlSinFirmar, byte[] certificadoP12, char[] passwordP12) {
        try {
            // 1) Cargar el certificado y la llave privada desde el .p12
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new ByteArrayInputStream(certificadoP12), passwordP12);
            String alias = keyStore.aliases().nextElement();
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, passwordP12);
            X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);

            // 2) Parsear el XML del comprobante a DOM
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document doc = dbf.newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xmlSinFirmar.getBytes("UTF-8")));

            // 3) Construir la firma enveloped (XML-DSig) sobre todo el documento
            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

            Reference ref = fac.newReference(
                    "",
                    fac.newDigestMethod(DigestMethod.SHA256, null),
                    List.of(fac.newTransform(Transform.ENVELOPED, (javax.xml.crypto.dsig.spec.TransformParameterSpec) null)),
                    null, null);

            SignedInfo signedInfo = fac.newSignedInfo(
                    fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
                    fac.newSignatureMethod(SignatureMethod.RSA_SHA256, null),
                    Collections.singletonList(ref));

            KeyInfoFactory kif = fac.getKeyInfoFactory();
            X509Data x509Data = kif.newX509Data(Collections.singletonList(certificate));
            KeyInfo keyInfo = kif.newKeyInfo(Collections.singletonList(x509Data));

            DOMSignContext signContext = new DOMSignContext(privateKey, doc.getDocumentElement());
            XMLSignature signature = fac.newXMLSignature(signedInfo, keyInfo);
            signature.sign(signContext);

            // 4) Serializar el documento firmado de vuelta a String
            StringWriter writer = new StringWriter();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.toString();

        } catch (Exception e) {
            throw new IllegalStateException("Error al firmar digitalmente el comprobante: " + e.getMessage(), e);
        }
    }
}
