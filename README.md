# Facturación Electrónica Costa Rica — v4.4

Código base (starter) para un sistema de facturación electrónica alineado con
la versión 4.4 de los Anexos y Estructuras del Ministerio de Hacienda de Costa Rica.

```
proyecto/
├── backend/      Java 21 + Spring Boot 3.3 (API REST)
├── frontend/     React 18 + TypeScript + Vite (SPA)
└── database/     Esquema y datos semilla en PostgreSQL
```

## 1. Base de datos (PostgreSQL)

```bash
createdb facturacion_electronica
psql facturacion_electronica -f database/schema.sql
psql facturacion_electronica -f database/seed.sql
```

El backend también aplica estos mismos scripts automáticamente vía **Flyway**
al arrancar (`backend/src/main/resources/db/migration`), por lo que el paso
manual anterior es opcional si se ejecuta primero el backend.

## 2. Backend (Java / Spring Boot)

Requisitos: JDK 21, Maven 3.9+, PostgreSQL 15+, Redis (opcional en desarrollo).

```bash
cd backend

mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080/api/v1`.
Documentación interactiva (Swagger UI): `http://localhost:8080/api/v1/docs`.

### Puntos de extensión antes de producción

Este starter deja explícitamente marcados con `TODO` los siguientes puntos,
necesarios para operar en producción pero fuera del alcance de un código base:

- **Certificados criptográficos**: lectura y desencriptado real del `.p12`
  del emisor (`CertificadoCriptografico` en la base de datos). Actualmente
  `ComprobanteService` lanza una excepción de ejemplo en su lugar.
- **Credenciales ATV**: uso de `CredencialHacienda` en lugar de las
  credenciales de ejemplo usadas en `ComprobanteService.enviarAHacienda`.
- **Cacheo de tokens OAuth2**: persistir y reutilizar el token vigente
  (tabla `token_oauth_cache` / Redis) en lugar de solicitar uno nuevo en
  cada envío.
- **Firma XAdES-EPES completa**: `XmlSignerServiceImpl` implementa una firma
  XML-DSig válida y funcional; para cumplir el perfil exigido por Hacienda
  falta añadir el bloque `xades:QualifyingProperties` (ver comentarios en
  esa clase).
- **Validación contra XSD oficiales** antes de firmar.
- **Cola de reintentos** con backoff exponencial para envíos fallidos
  (actualmente el reintento es manual vía `POST /comprobantes/{clave}/reenviar`).

## 3. Frontend (React + TypeScript)

Requisitos: Node.js 18+.

```bash
cd frontend
npm install
npm run dev
```

La aplicación queda disponible en `http://localhost:5173` y proxyea las
llamadas `/api/*` hacia `http://localhost:8080` (ver `vite.config.ts`).

Para generar el build de producción:

```bash
npm run build
```

## 4. Flujo funcional cubierto por este starter

1. Alta de un **Emisor** (contribuyente) — página *Emisores*.
2. Alta de **Receptores** (clientes) y **Productos/Servicios** — páginas dedicadas.
3. Emisión de un comprobante (Factura, Tiquete, NC, ND, FEC, FEE, REP) desde
   *Nuevo comprobante*, con cálculo automático de IVA por línea.
4. El backend genera la clave numérica (50 dígitos) y el consecutivo (20 dígitos),
   construye el XML, lo firma (XML-DSig) y lo transmite al API de Hacienda.
5. Listado de comprobantes con consulta de estado y reenvío ante errores.

## 5. Seguridad

El backend está configurado como **Resource Server OAuth2** (`SecurityConfig`),
validando JWT emitidos por el proveedor de identidad configurado en
`spring.security.oauth2.resourceserver.jwt.issuer-uri` (por ejemplo Keycloak).
Ajustar `application.yml` con el `issuer-uri` real antes de desplegar.

## 6. Referencia

Este código base corresponde a la arquitectura descrita en el documento
"Facturación Electrónica CR v4.4 — Documentación completa" (Word) generado
previamente: alcance, requisitos funcionales/no funcionales, arquitectura y
documentación de API.
