package com.factuelectronica.api.service;

import com.factuelectronica.api.model.Ambiente;
import com.factuelectronica.api.model.TipoDocumento;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Genera la clave numérica de 50 dígitos y el consecutivo de 20 dígitos
 * conforme a la estructura definida por el Ministerio de Hacienda (v4.4).
 *
 * Estructura de la clave numérica (50 dígitos):
 *  País(3) + Día(2) + Mes(2) + Año(2) + Identificación emisor(12, rellenada con ceros)
 *  + Consecutivo(20) + Situación comprobante(1) + Código de seguridad(8)
 *
 * En un entorno productivo, el consecutivo debe generarse de forma atómica
 * (p.ej. con una secuencia de base de datos o SELECT ... FOR UPDATE) por
 * combinación de emisor + sucursal + terminal + tipo de documento, para
 * evitar colisiones bajo concurrencia.
 */
@Service
public class ClaveNumericaService {

    private static final String PAIS = "506"; // Costa Rica
    private final SecureRandom random = new SecureRandom();

    // Demostrativo: en producción reemplazar por un contador persistente
    // (secuencia de PostgreSQL) por emisor/sucursal/terminal/tipoDocumento.
    private final AtomicLong contadorDemo = new AtomicLong(1);

    public String generarConsecutivo(String sucursal, String terminal, TipoDocumento tipoDocumento) {
        long siguiente = contadorDemo.getAndIncrement();
        // Consecutivo (20): Sucursal(3) + Terminal(5) + TipoDocumento(2) + Numeración(10)
        return sucursal
                + terminal
                + tipoDocumento.getCodigo()
                + String.format("%010d", siguiente);
    }

    public String generarClaveNumerica(String identificacionEmisor,
                                        String consecutivo,
                                        OffsetDateTime fechaEmision,
                                        Ambiente ambiente) {
        String dia = String.format("%02d", fechaEmision.getDayOfMonth());
        String mes = String.format("%02d", fechaEmision.getMonthValue());
        String anio = String.format("%02d", fechaEmision.getYear() % 100);
        String identificacion = normalizarIdentificacion(identificacionEmisor);
        String situacionComprobante = "1"; // 1 = normal, 2 = contingencia, 3 = sin internet
        String codigoSeguridad = generarCodigoSeguridad();

        String clave = PAIS + dia + mes + anio + identificacion + consecutivo
                + situacionComprobante + codigoSeguridad;

        if (clave.length() != 50) {
            throw new IllegalStateException(
                    "La clave numérica generada no tiene 50 dígitos (longitud=" + clave.length() + ")");
        }
        return clave;
    }

    private String normalizarIdentificacion(String identificacion) {
        // Rellena a 12 posiciones con ceros a la izquierda (soporta cédula física/jurídica).
        String soloNumeros = identificacion.replaceAll("[^0-9]", "");
        return String.format("%012d", Long.parseLong(soloNumeros.isEmpty() ? "0" : soloNumeros));
    }

    private String generarCodigoSeguridad() {
        int codigo = random.nextInt(100_000_000); // 8 dígitos
        return String.format("%08d", codigo);
    }
}
