import type { EstadoComprobante } from "@/types/domain";

export default function EstadoBadge({ estado }: { estado: EstadoComprobante }) {
  return <span className={`badge ${estado}`}>{estado.replace("_", " ")}</span>;
}
