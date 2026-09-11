import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { productosApi, type ProductoInput } from "@/api/productosApi";
import { useEmisorActivo } from "@/context/EmisorContext";
import SelectorEmisor from "@/components/SelectorEmisor";

const PRODUCTO_VACIO: Omit<ProductoInput, "emisorId"> = {
  codigoCabys: "",
  nombre: "",
  unidadMedida: "Unid",
  precioUnitario: 0,
  tarifaImpuesto: 13,
};

export default function ProductosPage() {
  const { emisorActivo } = useEmisorActivo();
  const [nuevo, setNuevo] = useState(PRODUCTO_VACIO);
  const queryClient = useQueryClient();

  const { data } = useQuery({
    queryKey: ["productos", emisorActivo?.id],
    queryFn: () => productosApi.listar(emisorActivo!.id),
    enabled: !!emisorActivo,
  });

  const crearMutation = useMutation({
    mutationFn: (input: ProductoInput) => productosApi.crear(input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["productos"] });
      setNuevo(PRODUCTO_VACIO);
    },
  });

  const handleCrear = (e: React.FormEvent) => {
    e.preventDefault();
    if (!emisorActivo) return;
    crearMutation.mutate({ ...nuevo, emisorId: emisorActivo.id });
  };

  return (
    <div>
      <h2>Catálogo de productos y servicios</h2>
      <div className="card">
        <SelectorEmisor />
      </div>

      <form className="card" onSubmit={handleCrear}>
        <h3>Nuevo producto</h3>
        <div className="form-grid">
          <div className="form-row">
            <label>Código CABYS (13 dígitos)</label>
            <input
              required
              pattern="\d{13}"
              value={nuevo.codigoCabys}
              onChange={(e) => setNuevo({ ...nuevo, codigoCabys: e.target.value })}
            />
          </div>
          <div className="form-row">
            <label>Nombre</label>
            <input required value={nuevo.nombre} onChange={(e) => setNuevo({ ...nuevo, nombre: e.target.value })} />
          </div>
          <div className="form-row">
            <label>Unidad de medida</label>
            <input
              required
              value={nuevo.unidadMedida}
              onChange={(e) => setNuevo({ ...nuevo, unidadMedida: e.target.value })}
            />
          </div>
          <div className="form-row">
            <label>Precio unitario</label>
            <input
              type="number"
              min={0}
              step="0.01"
              required
              value={nuevo.precioUnitario}
              onChange={(e) => setNuevo({ ...nuevo, precioUnitario: Number(e.target.value) })}
            />
          </div>
          <div className="form-row">
            <label>Tarifa de impuesto (%)</label>
            <input
              type="number"
              min={0}
              step="0.01"
              value={nuevo.tarifaImpuesto}
              onChange={(e) => setNuevo({ ...nuevo, tarifaImpuesto: Number(e.target.value) })}
            />
          </div>
        </div>
        <button type="submit" disabled={!emisorActivo || crearMutation.isPending}>
          Guardar producto
        </button>
      </form>

      <div className="card">
        <table>
          <thead>
            <tr>
              <th>CABYS</th>
              <th>Nombre</th>
              <th>Unidad</th>
              <th>Precio</th>
              <th>Impuesto</th>
            </tr>
          </thead>
          <tbody>
            {data?.content.map((p) => (
              <tr key={p.id}>
                <td>{p.codigoCabys}</td>
                <td>{p.nombre}</td>
                <td>{p.unidadMedida}</td>
                <td>{p.precioUnitario.toLocaleString("es-CR")}</td>
                <td>{p.tarifaImpuesto}%</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
