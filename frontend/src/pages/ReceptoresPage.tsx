import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { receptoresApi } from "@/api/receptoresApi";
import { useEmisorActivo } from "@/context/EmisorContext";
import SelectorEmisor from "@/components/SelectorEmisor";
import type { ReceptorInput } from "@/types/domain";

const RECEPTOR_VACIO: Omit<ReceptorInput, "emisorId"> = {
  tipoIdentificacion: "01",
  identificacion: "",
  nombre: "",
  correo: "",
};

export default function ReceptoresPage() {
  const { emisorActivo } = useEmisorActivo();
  const [busqueda, setBusqueda] = useState("");
  const [nuevo, setNuevo] = useState(RECEPTOR_VACIO);
  const queryClient = useQueryClient();

  const { data } = useQuery({
    queryKey: ["receptores", emisorActivo?.id, busqueda],
    queryFn: () => receptoresApi.listar(emisorActivo!.id, busqueda),
    enabled: !!emisorActivo,
  });

  const crearMutation = useMutation({
    mutationFn: (input: ReceptorInput) => receptoresApi.crear(input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["receptores"] });
      setNuevo(RECEPTOR_VACIO);
    },
  });

  const handleCrear = (e: React.FormEvent) => {
    e.preventDefault();
    if (!emisorActivo) return;
    crearMutation.mutate({ ...nuevo, emisorId: emisorActivo.id });
  };

  return (
    <div>
      <h2>Clientes (receptores)</h2>
      <div className="card">
        <SelectorEmisor />
      </div>

      <form className="card" onSubmit={handleCrear}>
        <h3>Nuevo receptor</h3>
        <div className="form-grid">
          <div className="form-row">
            <label>Tipo de identificación</label>
            <select
              value={nuevo.tipoIdentificacion}
              onChange={(e) => setNuevo({ ...nuevo, tipoIdentificacion: e.target.value })}
            >
              <option value="01">Física</option>
              <option value="02">Jurídica</option>
              <option value="03">DIMEX</option>
              <option value="04">NITE</option>
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
            <label>Nombre</label>
            <input required value={nuevo.nombre} onChange={(e) => setNuevo({ ...nuevo, nombre: e.target.value })} />
          </div>
          <div className="form-row">
            <label>Correo</label>
            <input
              type="email"
              value={nuevo.correo}
              onChange={(e) => setNuevo({ ...nuevo, correo: e.target.value })}
            />
          </div>
        </div>
        <button type="submit" disabled={!emisorActivo || crearMutation.isPending}>
          Guardar receptor
        </button>
      </form>

      <div className="card">
        <input
          placeholder="Buscar por nombre…"
          value={busqueda}
          onChange={(e) => setBusqueda(e.target.value)}
          style={{ marginBottom: 12 }}
        />
        <table>
          <thead>
            <tr>
              <th>Identificación</th>
              <th>Nombre</th>
              <th>Correo</th>
              <th>Estado</th>
            </tr>
          </thead>
          <tbody>
            {data?.content.map((r) => (
              <tr key={r.id}>
                <td>{r.identificacion}</td>
                <td>{r.nombre}</td>
                <td>{r.correo}</td>
                <td>{r.estado}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
