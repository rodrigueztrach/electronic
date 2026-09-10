import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { emisoresApi } from "@/api/emisoresApi";
import type { EmisorInput } from "@/types/domain";

const EMISOR_VACIO: EmisorInput = {
  tipoIdentificacion: "02",
  identificacion: "",
  razonSocial: "",
  nombreComercial: "",
  actividadEconomica: "",
  correoNotificacion: "",
  ambiente: "sandbox",
  proveedorSistemas: "",
};

export default function EmisoresPage() {
  const [nuevo, setNuevo] = useState<EmisorInput>(EMISOR_VACIO);
  const queryClient = useQueryClient();

  const { data: emisores } = useQuery({ queryKey: ["emisores"], queryFn: emisoresApi.listar });

  const crearMutation = useMutation({
    mutationFn: emisoresApi.crear,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["emisores"] });
      setNuevo(EMISOR_VACIO);
    },
  });

  const handleCrear = (e: React.FormEvent) => {
    e.preventDefault();
    crearMutation.mutate(nuevo);
  };

  return (
    <div>
      <h2>Emisores (contribuyentes)</h2>

      <form className="card" onSubmit={handleCrear}>
        <h3>Nuevo emisor</h3>
        <div className="form-grid">
          <div className="form-row">
            <label>Tipo de identificación</label>
            <select
              value={nuevo.tipoIdentificacion}
              onChange={(e) => setNuevo({ ...nuevo, tipoIdentificacion: e.target.value })}
            >
              <option value="01">Física</option>
              <option value="02">Jurídica</option>
            </select>
          </div>
          <div className="form-row">
            <label>Identificación</label>
            <input
              required
              value={nuevo.identificacion}
              onChange={(e) => setNuevo({ ...nuevo, identificacion: e.target.value })}
            />
          </div>
          <div className="form-row">
            <label>Razón social</label>
            <input
              required
              value={nuevo.razonSocial}
              onChange={(e) => setNuevo({ ...nuevo, razonSocial: e.target.value })}
            />
          </div>
          <div className="form-row">
            <label>Nombre comercial</label>
            <input
              value={nuevo.nombreComercial}
              onChange={(e) => setNuevo({ ...nuevo, nombreComercial: e.target.value })}
            />
          </div>
          <div className="form-row">
            <label>Actividad económica (CIIU)</label>
            <input
              required
              pattern="\d{4,6}"
              value={nuevo.actividadEconomica}
              onChange={(e) => setNuevo({ ...nuevo, actividadEconomica: e.target.value })}
            />
          </div>
          <div className="form-row">
            <label>Correo de notificación</label>
            <input
              type="email"
              value={nuevo.correoNotificacion}
              onChange={(e) => setNuevo({ ...nuevo, correoNotificacion: e.target.value })}
            />
          </div>
          <div className="form-row">
            <label>Ambiente</label>
            <select
              value={nuevo.ambiente}
              onChange={(e) => setNuevo({ ...nuevo, ambiente: e.target.value as "sandbox" | "produccion" })}
            >
              <option value="sandbox">Sandbox</option>
              <option value="produccion">Producción</option>
            </select>
          </div>
        </div>
        <button type="submit" disabled={crearMutation.isPending}>
          Guardar emisor
        </button>
      </form>

      <div className="card">
        <table>
          <thead>
            <tr>
              <th>Identificación</th>
              <th>Razón social</th>
              <th>Ambiente</th>
              <th>Versión esquema</th>
              <th>Estado</th>
            </tr>
          </thead>
          <tbody>
            {emisores?.map((e) => (
              <tr key={e.id}>
                <td>{e.identificacion}</td>
                <td>{e.razonSocial}</td>
                <td>{e.ambiente}</td>
                <td>{e.versionEsquema}</td>
                <td>{e.estado}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
