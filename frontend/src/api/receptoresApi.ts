import { httpClient } from "./httpClient";
import type { PaginaRespuesta, Receptor, ReceptorInput } from "@/types/domain";

export const receptoresApi = {
  listar: async (
    emisorId: string,
    nombre?: string,
    page = 0,
    size = 20
  ): Promise<PaginaRespuesta<Receptor>> => {
    const { data } = await httpClient.get<PaginaRespuesta<Receptor>>("/receptores", {
      params: { emisorId, nombre, page, size },
    });
    return data;
  },

  obtener: async (id: string): Promise<Receptor> => {
    const { data } = await httpClient.get<Receptor>(`/receptores/${id}`);
    return data;
  },

  crear: async (input: ReceptorInput): Promise<Receptor> => {
    const { data } = await httpClient.post<Receptor>("/receptores", input);
    return data;
  },

  actualizar: async (id: string, input: ReceptorInput): Promise<Receptor> => {
    const { data } = await httpClient.put<Receptor>(`/receptores/${id}`, input);
    return data;
  },

  inactivar: async (id: string): Promise<void> => {
    await httpClient.delete(`/receptores/${id}`);
  },
};
