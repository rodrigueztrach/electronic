import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import { useQuery } from "@tanstack/react-query";
import { emisoresApi } from "@/api/emisoresApi";
import type { Emisor } from "@/types/domain";

interface EmisorContextValue {
  emisores: Emisor[];
  emisorActivo: Emisor | null;
  seleccionarEmisor: (id: string) => void;
  cargando: boolean;
}

const EmisorContext = createContext<EmisorContextValue | undefined>(undefined);

const STORAGE_KEY = "fe_emisor_activo_id";

export function EmisorProvider({ children }: { children: ReactNode }) {
  const { data: emisores = [], isLoading } = useQuery({
    queryKey: ["emisores"],
    queryFn: emisoresApi.listar,
  });

  const [emisorActivoId, setEmisorActivoId] = useState<string | null>(
    () => window.localStorage.getItem(STORAGE_KEY)
  );

  useEffect(() => {
    if (!emisorActivoId && emisores.length > 0) {
      setEmisorActivoId(emisores[0].id);
    }
  }, [emisores, emisorActivoId]);

  const seleccionarEmisor = (id: string) => {
    setEmisorActivoId(id);
    window.localStorage.setItem(STORAGE_KEY, id);
  };

  const emisorActivo = emisores.find((e) => e.id === emisorActivoId) ?? null;

  return (
    <EmisorContext.Provider
      value={{ emisores, emisorActivo, seleccionarEmisor, cargando: isLoading }}
    >
      {children}
    </EmisorContext.Provider>
  );
}

export function useEmisorActivo(): EmisorContextValue {
  const ctx = useContext(EmisorContext);
  if (!ctx) {
    throw new Error("useEmisorActivo debe usarse dentro de <EmisorProvider>");
  }
  return ctx;
}
