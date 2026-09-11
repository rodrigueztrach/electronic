-- Datos semilla iniciales

INSERT INTO rol (nombre, descripcion) VALUES
    ('ADMIN', 'Administrador con acceso total'),
    ('FACTURADOR', 'Puede emitir y consultar comprobantes'),
    ('CONTADOR', 'Acceso a reportes y consultas fiscales'),
    ('LECTOR', 'Solo lectura');

INSERT INTO parametro_sistema (clave, valor, descripcion) VALUES
    ('hacienda.reintentos.max', '5', 'Número máximo de reintentos de envío a Hacienda'),
    ('hacienda.reintentos.backoff.base.segundos', '60', 'Base para backoff exponencial de reintentos'),
    ('token.oauth.margen.renovacion.pct', '80', 'Porcentaje de vida útil del token para disparar renovación anticipada');

-- Muestra de códigos CAByS (referencial; sincronizar con catálogo oficial en producción)
INSERT INTO cabys (codigo, descripcion, impuesto_default) VALUES
    ('8511100000000', 'Servicios de consultoría y asesoría empresarial', 13.00),
    ('4711000000000', 'Venta al por menor en comercios no especializados', 13.00),
    ('6201000000000', 'Servicios de desarrollo de software', 13.00);

-- Emisor de ejemplo (sandbox)
INSERT INTO emisor (id, tipo_identificacion, identificacion, razon_social, nombre_comercial, actividad_economica, correo_notificacion, ambiente, version_esquema, proveedor_sistemas)
VALUES ('11111111-1111-1111-1111-111111111111', '02', '3101123456', 'Comercial Ejemplo S.A.', 'Ejemplo Store', '4711', 'facturacion@ejemplo.cr', 'sandbox', '4.4', '9-3000-000000');
