import { httpClient } from "./httpClient";
import type { PaginaRespuesta, ProductoServicio } from "@/types/domain";

export interface ProductoInput {
  emisorId: string;
  codigoInterno?: string;
  codigoCabys: string;
  nombre: string;
  unidadMedida: string;
  precioUnitario: number;
  tarifaImpuesto?: number;
}

export const productosApi = {
  listar: async (emisorId: string, page = 0, size = 20): Promise<PaginaRespuesta<ProductoServicio>> => {
    const { data } = await httpClient.get<PaginaRespuesta<ProductoServicio>>("/productos", {
      params: { emisorId, page, size },
    });
    return data;
  },

  crear: async (input: ProductoInput): Promise<ProductoServicio> => {
    const { data } = await httpClient.post<ProductoServicio>("/productos", input);
    return data;
  },

  actualizar: async (id: string, input: ProductoInput): Promise<ProductoServicio> => {
    const { data } = await httpClient.put<ProductoServicio>(`/productos/${id}`, input);
    return data;
  },
};
