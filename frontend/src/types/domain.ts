// Tipos que reflejan los DTOs expuestos por la API Java (Spring Boot).

export type Ambiente = "sandbox" | "produccion";

export type TipoDocumento = "01" | "02" | "03" | "04" | "08" | "09" | "10";

export const NOMBRES_TIPO_DOCUMENTO: Record<TipoDocumento, string> = {
  "01": "Factura Electrónica",
  "02": "Nota de Débito",
  "03": "Nota de Crédito",
  "04": "Tiquete Electrónico",
  "08": "Factura Electrónica de Compra",
  "09": "Factura Electrónica de Exportación",
  "10": "Recibo Electrónico de Pago",
};

export type EstadoComprobante =
  | "creado"
  | "validado"
  | "firmado"
  | "enviado"
  | "aceptado"
  | "aceptado_parcial"
  | "rechazado"
  | "error_envio"
  | "anulado";

export interface Emisor {
  id: string;
  tipoIdentificacion: string;
  identificacion: string;
  razonSocial: string;
  nombreComercial?: string;
  actividadEconomica: string;
  ambiente: Ambiente;
  versionEsquema: string;
  estado: string;
  creadoEn: string;
}

export interface EmisorInput {
  tipoIdentificacion: string;
  identificacion: string;
  razonSocial: string;
  nombreComercial?: string;
  actividadEconomica: string;
  correoNotificacion?: string;
  ambiente: Ambiente;
  proveedorSistemas?: string;
}

export interface Receptor {
  id: string;
  tipoIdentificacion: string;
  identificacion: string;
  nombre: string;
  correo?: string;
  estado: string;
}

export interface ReceptorInput {
  emisorId: string;
  tipoIdentificacion: string;
  identificacion: string;
  nombre: string;
  correo?: string;
  telefono?: string;
  codigoActividadReceptor?: string;
  provincia?: string;
  canton?: string;
  distrito?: string;
  otrasSenas?: string;
}

export interface ProductoServicio {
  id: string;
  codigoInterno?: string;
  codigoCabys: string;
  nombre: string;
  unidadMedida: string;
  precioUnitario: number;
  tarifaImpuesto: number;
  estado: string;
}

export interface ImpuestoLineaInput {
  codigo: string;
  codigoTarifaIva?: string;
  tarifa: number;
  monto: number;
}

export interface DetalleLineaInput {
  numeroLinea: number;
  codigoCabys: string;
  cantidad: number;
  unidadMedida: string;
  detalle: string;
  precioUnitario: number;
  montoDescuento?: number;
  naturalezaDescuento?: string;
  impuestos: ImpuestoLineaInput[];
}

export interface ComprobanteInput {
  emisorId: string;
  tipoDocumento: TipoDocumento;
  condicionVenta: string;
  plazoCredito?: string;
  medioPago?: string[];
  moneda: string;
  tipoCambio?: number;
  receptor: {
    id?: string;
    tipoIdentificacion: string;
    identificacion: string;
    nombre: string;
    correo?: string;
    codigoActividadReceptor?: string;
  };
  detalle: DetalleLineaInput[];
  proveedorSistemas?: string;
}

export interface Comprobante {
  id: string;
  claveNumerica: string;
  consecutivo: string;
  tipoDocumento: TipoDocumento;
  estado: EstadoComprobante;
  fechaEmision: string;
  moneda: string;
  totalComprobante: number;
  motivoError?: string;
}

export interface PaginaRespuesta<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
