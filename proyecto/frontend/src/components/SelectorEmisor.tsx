import { useEmisorActivo } from "@/context/EmisorContext";

export default function SelectorEmisor() {
  const { emisores, emisorActivo, seleccionarEmisor, cargando } = useEmisorActivo();

  if (cargando) return <p>Cargando emisores…</p>;
  if (emisores.length === 0) {
    return <p>No hay emisores registrados. Cree uno en la sección "Emisores".</p>;
  }

  return (
    <div className="form-row" style={{ maxWidth: 320 }}>
      <label htmlFor="selector-emisor">Emisor activo</label>
      <select
        id="selector-emisor"
        value={emisorActivo?.id ?? ""}
        onChange={(e) => seleccionarEmisor(e.target.value)}
      >
        {emisores.map((e) => (
          <option key={e.id} value={e.id}>
            {e.razonSocial} ({e.ambiente})
          </option>
        ))}
      </select>
    </div>
  );
}
