import { httpClient } from "./httpClient";
import type {
  Comprobante,
  ComprobanteInput,
  EstadoComprobante,
  PaginaRespuesta,
} from "@/types/domain";

export const comprobantesApi = {
  listar: async (
    emisorId: string,
    estado?: EstadoComprobante,
    page = 0,
    size = 20
  ): Promise<PaginaRespuesta<Comprobante>> => {
    const { data } = await httpClient.get<PaginaRespuesta<Comprobante>>("/comprobantes", {
      params: { emisorId, estado, page, size },
    });
    return data;
  },

  obtener: async (claveNumerica: string): Promise<Comprobante> => {
    const { data } = await httpClient.get<Comprobante>(`/comprobantes/${claveNumerica}`);
    return data;
  },

  crear: async (input: ComprobanteInput, idempotencyKey: string): Promise<Comprobante> => {
    const { data } = await httpClient.post<Comprobante>("/comprobantes", input, {
      headers: { "Idempotency-Key": idempotencyKey },
    });
    return data;
  },

  consultarEstado: async (claveNumerica: string): Promise<Comprobante> => {
    const { data } = await httpClient.get<Comprobante>(`/comprobantes/${claveNumerica}/estado`);
    return data;
  },

  reenviar: async (claveNumerica: string): Promise<Comprobante> => {
    const { data } = await httpClient.post<Comprobante>(`/comprobantes/${claveNumerica}/reenviar`);
    return data;
  },

  descargarXmlUrl: (claveNumerica: string): string => `/api/v1/comprobantes/${claveNumerica}/xml`,
};
