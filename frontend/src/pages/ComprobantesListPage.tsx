import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { comprobantesApi } from "@/api/comprobantesApi";
import { useEmisorActivo } from "@/context/EmisorContext";
import SelectorEmisor from "@/components/SelectorEmisor";
import EstadoBadge from "@/components/EstadoBadge";
import { NOMBRES_TIPO_DOCUMENTO, type EstadoComprobante } from "@/types/domain";

export default function ComprobantesListPage() {
  const { emisorActivo } = useEmisorActivo();
  const [estadoFiltro, setEstadoFiltro] = useState<EstadoComprobante | "">("");
  const [pagina, setPagina] = useState(0);
  const queryClient = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: ["comprobantes", emisorActivo?.id, estadoFiltro, pagina],
    queryFn: () =>
      comprobantesApi.listar(
        emisorActivo!.id,
        estadoFiltro || undefined,
        pagina,
        20
      ),
    enabled: !!emisorActivo,
  });

  const reenviarMutation = useMutation({
    mutationFn: comprobantesApi.reenviar,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["comprobantes"] }),
  });

  const consultarEstadoMutation = useMutation({
    mutationFn: comprobantesApi.consultarEstado,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["comprobantes"] }),
  });

  return (
    <div>
      <h2>Comprobantes emitidos</h2>
      <div className="card">
        <div className="form-grid">
          <SelectorEmisor />
          <div className="form-row">
            <label htmlFor="filtro-estado">Filtrar por estado</label>
            <select
              id="filtro-estado"
              value={estadoFiltro}
              onChange={(e) => {
                setPagina(0);
                setEstadoFiltro(e.target.value as EstadoComprobante | "");
              }}
            >
              <option value="">Todos</option>
              <option value="enviado">Enviado</option>
              <option value="aceptado">Aceptado</option>
              <option value="rechazado">Rechazado</option>
              <option value="error_envio">Error de envío</option>
              <option value="anulado">Anulado</option>
            </select>
          </div>
        </div>
      </div>

      <div className="card">
        {isLoading && <p>Cargando comprobantes…</p>}
        {!isLoading && (data?.content.length ?? 0) === 0 && <p>No hay comprobantes para mostrar.</p>}

        {(data?.content.length ?? 0) > 0 && (
          <table>
            <thead>
              <tr>
                <th>Clave numérica</th>
                <th>Tipo</th>
                <th>Fecha</th>
                <th>Total</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {data!.content.map((c) => (
                <tr key={c.id}>
                  <td style={{ fontFamily: "monospace", fontSize: 12 }}>{c.claveNumerica}</td>
                  <td>{NOMBRES_TIPO_DOCUMENTO[c.tipoDocumento]}</td>
                  <td>{new Date(c.fechaEmision).toLocaleString("es-CR")}</td>
                  <td>
                    {c.moneda} {c.totalComprobante.toLocaleString("es-CR")}
                  </td>
                  <td>
                    <EstadoBadge estado={c.estado} />
                  </td>
                  <td style={{ display: "flex", gap: 6 }}>
                    <button onClick={() => consultarEstadoMutation.mutate(c.claveNumerica)}>
                      Consultar
                    </button>
                    {c.estado === "error_envio" && (
                      <button onClick={() => reenviarMutation.mutate(c.claveNumerica)}>
                        Reenviar
                      </button>
                    )}
                    <a className="btn" href={comprobantesApi.descargarXmlUrl(c.claveNumerica)}>
                      XML
                    </a>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {data && data.totalPages > 1 && (
          <div style={{ marginTop: 12, display: "flex", gap: 8 }}>
            <button disabled={pagina === 0} onClick={() => setPagina((p) => p - 1)}>
              Anterior
            </button>
            <span>
              Página {pagina + 1} de {data.totalPages}
            </span>
            <button disabled={pagina + 1 >= data.totalPages} onClick={() => setPagina((p) => p + 1)}>
              Siguiente
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
