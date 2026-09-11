import { httpClient } from "./httpClient";
import type { Emisor, EmisorInput } from "@/types/domain";

export const emisoresApi = {
  listar: async (): Promise<Emisor[]> => {
    const { data } = await httpClient.get<Emisor[]>("/emisores");
    return data;
  },

  obtener: async (id: string): Promise<Emisor> => {
    const { data } = await httpClient.get<Emisor>(`/emisores/${id}`);
    return data;
  },

  crear: async (input: EmisorInput): Promise<Emisor> => {
    const { data } = await httpClient.post<Emisor>("/emisores", input);
    return data;
  },

  actualizar: async (id: string, input: EmisorInput): Promise<Emisor> => {
    const { data } = await httpClient.put<Emisor>(`/emisores/${id}`, input);
    return data;
  },

  cambiarEstado: async (id: string, estado: "activo" | "inactivo"): Promise<void> => {
    await httpClient.patch(`/emisores/${id}/estado`, { estado });
  },
};
