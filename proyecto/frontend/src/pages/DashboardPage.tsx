import { useQuery } from "@tanstack/react-query";
import { comprobantesApi } from "@/api/comprobantesApi";
import { useEmisorActivo } from "@/context/EmisorContext";
import SelectorEmisor from "@/components/SelectorEmisor";
import type { EstadoComprobante } from "@/types/domain";

const ESTADOS_RESUMEN: EstadoComprobante[] = ["aceptado", "enviado", "rechazado", "error_envio"];

export default function DashboardPage() {
  const { emisorActivo } = useEmisorActivo();

  const consultas = ESTADOS_RESUMEN.map((estado) =>
    useQuery({
      queryKey: ["resumen-comprobantes", emisorActivo?.id, estado],
      queryFn: () => comprobantesApi.listar(emisorActivo!.id, estado, 0, 1),
      enabled: !!emisorActivo,
    })
  );

  return (
    <div>
      <h2>Panel de estado</h2>
      <div className="card">
        <SelectorEmisor />
      </div>

      {emisorActivo && (
        <div className="form-grid">
          {ESTADOS_RESUMEN.map((estado, idx) => (
            <div className="card" key={estado}>
              <label>{estado.replace("_", " ").toUpperCase()}</label>
              <p style={{ fontSize: 28, fontWeight: 700, margin: 0 }}>
                {consultas[idx].data?.totalElements ?? "…"}
              </p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
