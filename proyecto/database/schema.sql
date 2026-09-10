-- ============================================================================
-- Facturación Electrónica Costa Rica v4.4 — Esquema de Base de Datos (PostgreSQL)
-- ============================================================================
-- Requiere extensión pgcrypto para gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ----------------------------------------------------------------------------
-- Catálogos generales
-- ----------------------------------------------------------------------------
CREATE TABLE cabys (
    codigo              VARCHAR(13) PRIMARY KEY,
    descripcion         VARCHAR(500) NOT NULL,
    impuesto_default    NUMERIC(5,2) NOT NULL DEFAULT 13.00,
    vigente             BOOLEAN NOT NULL DEFAULT TRUE,
    actualizado_en      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE tipo_cambio (
    id                  BIGSERIAL PRIMARY KEY,
    moneda              CHAR(3) NOT NULL,
    fecha               DATE NOT NULL,
    valor               NUMERIC(18,5) NOT NULL,
    UNIQUE (moneda, fecha)
);

-- ----------------------------------------------------------------------------
-- Seguridad: usuarios, roles, permisos
-- ----------------------------------------------------------------------------
CREATE TABLE rol (
    id                  SMALLSERIAL PRIMARY KEY,
    nombre              VARCHAR(50) NOT NULL UNIQUE,
    descripcion         VARCHAR(255)
);

CREATE TABLE usuario (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    correo              VARCHAR(150) NOT NULL UNIQUE,
    nombre_completo     VARCHAR(200) NOT NULL,
    password_hash       VARCHAR(255) NOT NULL,
    estado              VARCHAR(15) NOT NULL DEFAULT 'activo',
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ----------------------------------------------------------------------------
-- Emisores (contribuyentes)
-- ----------------------------------------------------------------------------
CREATE TABLE emisor (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo_identificacion     VARCHAR(2)  NOT NULL,          -- 01,02,03,04,05,06
    identificacion          VARCHAR(20) NOT NULL UNIQUE,   -- soporta cédula jurídica alfanumérica
    razon_social            VARCHAR(255) NOT NULL,
    nombre_comercial        VARCHAR(255),
    actividad_economica     VARCHAR(10) NOT NULL,          -- código CIIU
    correo_notificacion     VARCHAR(150),
    ambiente                VARCHAR(10)  NOT NULL DEFAULT 'sandbox',  -- sandbox | produccion
    version_esquema         VARCHAR(5)   NOT NULL DEFAULT '4.4',
    proveedor_sistemas      VARCHAR(20),
    estado                  VARCHAR(15)  NOT NULL DEFAULT 'activo',
    creado_en               TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_en          TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE usuario_rol (
    usuario_id  UUID NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    rol_id      SMALLINT NOT NULL REFERENCES rol(id) ON DELETE CASCADE,
    emisor_id   UUID REFERENCES emisor(id) ON DELETE CASCADE, -- NULL = alcance global
    PRIMARY KEY (usuario_id, rol_id, emisor_id)
);

-- Certificado criptográfico (.p12) por emisor — contenido cifrado en aplicación
CREATE TABLE certificado_criptografico (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    emisor_id           UUID NOT NULL REFERENCES emisor(id) ON DELETE CASCADE,
    alias               VARCHAR(100),
    contenido_cifrado   BYTEA NOT NULL,          -- .p12 cifrado con clave de aplicación (AES-256)
    password_cifrada    VARCHAR(500) NOT NULL,   -- contraseña del .p12, cifrada
    valido_desde        DATE NOT NULL,
    valido_hasta        DATE NOT NULL,
    estado              VARCHAR(15) NOT NULL DEFAULT 'activo',
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_certificado_emisor ON certificado_criptografico(emisor_id);

-- Credenciales de acceso al API de Hacienda (usuario/clave ATV) por emisor y ambiente
CREATE TABLE credencial_hacienda (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    emisor_id           UUID NOT NULL REFERENCES emisor(id) ON DELETE CASCADE,
    ambiente            VARCHAR(10) NOT NULL,             -- sandbox | produccion
    usuario_atv         VARCHAR(100) NOT NULL,
    password_cifrada    VARCHAR(500) NOT NULL,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (emisor_id, ambiente)
);

-- Cache de tokens OAuth2 obtenidos del IdP de Hacienda
CREATE TABLE token_oauth_cache (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    emisor_id           UUID NOT NULL REFERENCES emisor(id) ON DELETE CASCADE,
    ambiente            VARCHAR(10) NOT NULL,
    access_token        TEXT NOT NULL,
    refresh_token       TEXT,
    expira_en           TIMESTAMPTZ NOT NULL,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (emisor_id, ambiente)
);

-- ----------------------------------------------------------------------------
-- Receptores (clientes)
-- ----------------------------------------------------------------------------
CREATE TABLE receptor (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    emisor_id                   UUID NOT NULL REFERENCES emisor(id) ON DELETE CASCADE,
    tipo_identificacion         VARCHAR(2) NOT NULL,
    identificacion              VARCHAR(20) NOT NULL,
    nombre                      VARCHAR(255) NOT NULL,
    correo                      VARCHAR(150),
    telefono                    VARCHAR(30),
    codigo_actividad_receptor   VARCHAR(10),
    provincia                   VARCHAR(2),
    canton                      VARCHAR(2),
    distrito                    VARCHAR(2),
    otras_senas                 VARCHAR(500),
    estado                      VARCHAR(15) NOT NULL DEFAULT 'activo',
    creado_en                   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (emisor_id, identificacion)
);
CREATE INDEX idx_receptor_emisor ON receptor(emisor_id);
CREATE INDEX idx_receptor_nombre ON receptor USING gin (to_tsvector('spanish', nombre));

-- ----------------------------------------------------------------------------
-- Catálogo interno de productos / servicios
-- ----------------------------------------------------------------------------
CREATE TABLE producto_servicio (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    emisor_id           UUID NOT NULL REFERENCES emisor(id) ON DELETE CASCADE,
    codigo_interno      VARCHAR(50),
    codigo_cabys        VARCHAR(13) NOT NULL REFERENCES cabys(codigo),
    nombre              VARCHAR(255) NOT NULL,
    unidad_medida       VARCHAR(10) NOT NULL DEFAULT 'Unid',
    precio_unitario     NUMERIC(18,5) NOT NULL DEFAULT 0,
    tarifa_impuesto     NUMERIC(5,2) NOT NULL DEFAULT 13.00,
    registro_medicamento VARCHAR(50),   -- campo nuevo v4.4, sector farmacéutico
    forma_farmaceutica   VARCHAR(50),   -- campo nuevo v4.4
    estado              VARCHAR(15) NOT NULL DEFAULT 'activo',
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_producto_emisor ON producto_servicio(emisor_id);

-- ----------------------------------------------------------------------------
-- Comprobantes electrónicos (tabla particionada por fecha de emisión)
-- ----------------------------------------------------------------------------
CREATE TABLE comprobante (
    id                  UUID NOT NULL DEFAULT gen_random_uuid(),
    emisor_id           UUID NOT NULL REFERENCES emisor(id),
    receptor_id         UUID REFERENCES receptor(id),
    tipo_documento      VARCHAR(2) NOT NULL,       -- 01 FE,02 ND,03 NC,04 TE,08 FEC,09 FEE,10 REP
    clave_numerica      CHAR(50) NOT NULL,
    consecutivo         CHAR(20) NOT NULL,
    sucursal            VARCHAR(3) NOT NULL DEFAULT '001',
    terminal            VARCHAR(5) NOT NULL DEFAULT '00001',
    fecha_emision       TIMESTAMPTZ NOT NULL,
    moneda              CHAR(3) NOT NULL DEFAULT 'CRC',
    tipo_cambio         NUMERIC(18,5),
    condicion_venta     VARCHAR(2) NOT NULL DEFAULT '01',
    plazo_credito       VARCHAR(10),
    medio_pago          VARCHAR(2)[],
    total_gravado       NUMERIC(18,5) NOT NULL DEFAULT 0,
    total_exento        NUMERIC(18,5) NOT NULL DEFAULT 0,
    total_exonerado     NUMERIC(18,5) NOT NULL DEFAULT 0,
    total_impuesto      NUMERIC(18,5) NOT NULL DEFAULT 0,
    total_descuento     NUMERIC(18,5) NOT NULL DEFAULT 0,
    total_comprobante   NUMERIC(18,5) NOT NULL DEFAULT 0,
    estado              VARCHAR(20) NOT NULL DEFAULT 'creado',
    -- creado -> validado -> firmado -> enviado -> aceptado/aceptado_parcial/rechazado/error_envio -> anulado
    version_esquema     VARCHAR(5) NOT NULL DEFAULT '4.4',
    proveedor_sistemas  VARCHAR(20),
    xml_firmado         TEXT,                 -- o referencia a object storage en producción
    xml_hash            VARCHAR(128),
    motivo_error        VARCHAR(500),
    creado_por          UUID REFERENCES usuario(id),
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en      TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (id, fecha_emision),
    UNIQUE (clave_numerica, fecha_emision),
    UNIQUE (consecutivo, fecha_emision)
) PARTITION BY RANGE (fecha_emision);

-- Particiones de ejemplo (crear vía job programado mensualmente)
CREATE TABLE comprobante_2026_09 PARTITION OF comprobante
    FOR VALUES FROM ('2026-09-01') TO ('2026-10-01');
CREATE TABLE comprobante_2026_10 PARTITION OF comprobante
    FOR VALUES FROM ('2026-10-01') TO ('2026-11-01');
CREATE TABLE comprobante_default PARTITION OF comprobante DEFAULT;

CREATE INDEX idx_comprobante_estado ON comprobante(estado);
CREATE INDEX idx_comprobante_emisor_fecha ON comprobante(emisor_id, fecha_emision);
CREATE INDEX idx_comprobante_receptor ON comprobante(receptor_id);

-- Detalle de líneas del comprobante
CREATE TABLE comprobante_detalle (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    comprobante_id      UUID NOT NULL,
    comprobante_fecha   TIMESTAMPTZ NOT NULL,
    numero_linea        INTEGER NOT NULL,
    codigo_cabys        VARCHAR(13) NOT NULL,
    codigo_comercial    VARCHAR(50),
    cantidad            NUMERIC(18,5) NOT NULL DEFAULT 1,
    unidad_medida       VARCHAR(10) NOT NULL DEFAULT 'Unid',
    detalle             VARCHAR(500) NOT NULL,
    precio_unitario     NUMERIC(18,5) NOT NULL,
    monto_descuento     NUMERIC(18,5) NOT NULL DEFAULT 0,
    naturaleza_descuento VARCHAR(80),
    subtotal            NUMERIC(18,5) NOT NULL,
    monto_total_linea   NUMERIC(18,5) NOT NULL,
    tipo_transaccion    VARCHAR(2),           -- campo nuevo v4.4: impuestos específicos
    FOREIGN KEY (comprobante_id, comprobante_fecha) REFERENCES comprobante(id, fecha_emision) ON DELETE CASCADE
);
CREATE INDEX idx_detalle_comprobante ON comprobante_detalle(comprobante_id, comprobante_fecha);

-- Impuestos por línea de detalle
CREATE TABLE comprobante_impuesto (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    detalle_id          UUID NOT NULL REFERENCES comprobante_detalle(id) ON DELETE CASCADE,
    codigo_impuesto     VARCHAR(2) NOT NULL,   -- 01 = IVA
    codigo_tarifa_iva   VARCHAR(2),
    tarifa              NUMERIC(5,2) NOT NULL,
    monto               NUMERIC(18,5) NOT NULL,
    -- Exoneración (opcional)
    exoneracion_tipo_doc    VARCHAR(2),
    exoneracion_numero_doc  VARCHAR(40),
    exoneracion_institucion VARCHAR(50),
    exoneracion_fecha       DATE,
    exoneracion_porcentaje  NUMERIC(5,2),
    exoneracion_monto       NUMERIC(18,5)
);
CREATE INDEX idx_impuesto_detalle ON comprobante_impuesto(detalle_id);

-- Referencias a otros comprobantes (NC / ND / REP)
CREATE TABLE comprobante_referencia (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    comprobante_id          UUID NOT NULL,
    comprobante_fecha       TIMESTAMPTZ NOT NULL,
    tipo_documento_ref      VARCHAR(2) NOT NULL,
    numero_documento_ref    CHAR(50) NOT NULL,
    fecha_emision_ref       TIMESTAMPTZ NOT NULL,
    codigo_razon            VARCHAR(2) NOT NULL,   -- 01 anula documento, 02 corrige monto, etc.
    razon                   VARCHAR(255),
    FOREIGN KEY (comprobante_id, comprobante_fecha) REFERENCES comprobante(id, fecha_emision) ON DELETE CASCADE
);

-- Mensajes / respuestas del API de Hacienda
CREATE TABLE mensaje_hacienda (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    comprobante_id      UUID NOT NULL,
    comprobante_fecha   TIMESTAMPTZ NOT NULL,
    ind_estado          VARCHAR(20) NOT NULL,   -- recibido, procesando, aceptado, rechazado, error
    xml_respuesta       TEXT,
    detalle_error       VARCHAR(1000),
    fecha_respuesta     TIMESTAMPTZ NOT NULL DEFAULT now(),
    FOREIGN KEY (comprobante_id, comprobante_fecha) REFERENCES comprobante(id, fecha_emision) ON DELETE CASCADE
);
CREATE INDEX idx_mensaje_comprobante ON mensaje_hacienda(comprobante_id, comprobante_fecha);

-- ----------------------------------------------------------------------------
-- Webhooks
-- ----------------------------------------------------------------------------
CREATE TABLE webhook_config (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    emisor_id       UUID NOT NULL REFERENCES emisor(id) ON DELETE CASCADE,
    url             VARCHAR(500) NOT NULL,
    eventos         VARCHAR(50)[] NOT NULL,     -- ej. {comprobante.aceptado, comprobante.rechazado}
    secreto_hmac    VARCHAR(255) NOT NULL,
    estado          VARCHAR(15) NOT NULL DEFAULT 'activo',
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE webhook_entrega (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    webhook_id          UUID NOT NULL REFERENCES webhook_config(id) ON DELETE CASCADE,
    comprobante_id      UUID NOT NULL,
    evento              VARCHAR(50) NOT NULL,
    codigo_respuesta    INTEGER,
    intento             INTEGER NOT NULL DEFAULT 1,
    exitoso             BOOLEAN NOT NULL DEFAULT FALSE,
    enviado_en          TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ----------------------------------------------------------------------------
-- Auditoría (append-only)
-- ----------------------------------------------------------------------------
CREATE TABLE evento_bitacora (
    id              BIGSERIAL PRIMARY KEY,
    usuario_id      UUID REFERENCES usuario(id),
    emisor_id       UUID REFERENCES emisor(id),
    entidad         VARCHAR(100) NOT NULL,
    entidad_id      VARCHAR(100),
    accion          VARCHAR(50) NOT NULL,
    detalle         JSONB,
    ip_origen       VARCHAR(45),
    ocurrido_en     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_bitacora_entidad ON evento_bitacora(entidad, entidad_id);
CREATE INDEX idx_bitacora_fecha ON evento_bitacora(ocurrido_en);

-- ----------------------------------------------------------------------------
-- Parámetros de sistema
-- ----------------------------------------------------------------------------
CREATE TABLE parametro_sistema (
    clave       VARCHAR(100) PRIMARY KEY,
    valor       VARCHAR(500) NOT NULL,
    descripcion VARCHAR(255)
);
