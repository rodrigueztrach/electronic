import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { comprobantesApi } from "@/api/comprobantesApi";
import { useEmisorActivo } from "@/context/EmisorContext";
import SelectorEmisor from "@/components/SelectorEmisor";
import type { ComprobanteInput, DetalleLineaInput, TipoDocumento } from "@/types/domain";
import { NOMBRES_TIPO_DOCUMENTO } from "@/types/domain";

const TARIFA_IVA_DEFAULT = 13;

function lineaVacia(numero: number): DetalleLineaInput {
  return {
    numeroLinea: numero,
    codigoCabys: "",
    cantidad: 1,
    unidadMedida: "Unid",
    detalle: "",
    precioUnitario: 0,
    montoDescuento: 0,
    impuestos: [{ codigo: "01", tarifa: TARIFA_IVA_DEFAULT, monto: 0 }],
  };
}

export default function NuevoComprobantePage() {
  const { emisorActivo } = useEmisorActivo();
  const navigate = useNavigate();

  const [tipoDocumento, setTipoDocumento] = useState<TipoDocumento>("01");
  const [condicionVenta, setCondicionVenta] = useState("01");
  const [moneda, setMoneda] = useState("CRC");
  const [receptorNombre, setReceptorNombre] = useState("");
  const [receptorIdentificacion, setReceptorIdentificacion] = useState("");
  const [receptorTipoId, setReceptorTipoId] = useState("01");
  const [receptorCorreo, setReceptorCorreo] = useState("");
  const [lineas, setLineas] = useState<DetalleLineaInput[]>([lineaVacia(1)]);

  const crearMutation = useMutation({
    mutationFn: (input: ComprobanteInput) =>
      comprobantesApi.crear(input, crypto.randomUUID()),
    onSuccess: () => {
      navigate("/comprobantes");
    },
  });

  const actualizarLinea = (idx: number, cambios: Partial<DetalleLineaInput>) => {
    setLineas((prev) => {
      const copia = [...prev];
      const actualizada = { ...copia[idx], ...cambios };

      // Recalcula el monto del IVA automáticamente si cambia cantidad/precio.
      const subtotal = actualizada.precioUnitario * actualizada.cantidad - (actualizada.montoDescuento ?? 0);
      const tarifa = actualizada.impuestos[0]?.tarifa ?? TARIFA_IVA_DEFAULT;
      actualizada.impuestos = [{ codigo: "01", tarifa, monto: Number(((subtotal * tarifa) / 100).toFixed(5)) }];

      copia[idx] = actualizada;
      return copia;
    });
  };

  const agregarLinea = () => setLineas((prev) => [...prev, lineaVacia(prev.length + 1)]);
  const quitarLinea = (idx: number) =>
    setLineas((prev) => prev.filter((_, i) => i !== idx).map((l, i) => ({ ...l, numeroLinea: i + 1 })));

  const totalEstimado = lineas.reduce((acc, l) => {
    const subtotal = l.precioUnitario * l.cantidad - (l.montoDescuento ?? 0);
    const impuestos = l.impuestos.reduce((a, i) => a + i.monto, 0);
    return acc + subtotal + impuestos;
  }, 0);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!emisorActivo) return;

    const input: ComprobanteInput = {
      emisorId: emisorActivo.id,
      tipoDocumento,
      condicionVenta,
      moneda,
      receptor: {
        tipoIdentificacion: receptorTipoId,
        identificacion: receptorIdentificacion,
        nombre: receptorNombre,
        correo: receptorCorreo || undefined,
      },
      detalle: lineas,
    };
    crearMutation.mutate(input);
  };

  return (
    <div>
      <h2>Nuevo comprobante</h2>

      <div className="card">
        <SelectorEmisor />
      </div>

      <form className="card" onSubmit={handleSubmit}>
        <div className="form-grid">
          <div className="form-row">
            <label htmlFor="tipo-documento">Tipo de documento</label>
            <select
              id="tipo-documento"
              value={tipoDocumento}
              onChange={(e) => setTipoDocumento(e.target.value as TipoDocumento)}
            >
              {Object.entries(NOMBRES_TIPO_DOCUMENTO).map(([codigo, nombre]) => (
                <option key={codigo} value={codigo}>
                  {nombre}
                </option>
              ))}
            </select>
          </div>

          <div className="form-row">
            <label htmlFor="condicion-venta">Condición de venta</label>
            <select id="condicion-venta" value={condicionVenta} onChange={(e) => setCondicionVenta(e.target.value)}>
              <option value="01">Contado</option>
              <option value="02">Crédito</option>
              <option value="03">Consignación</option>
              <option value="04">Apartado</option>
            </select>
          </div>

          <div className="form-row">
            <label htmlFor="moneda">Moneda</label>
            <select id="moneda" value={moneda} onChange={(e) => setMoneda(e.target.value)}>
              <option value="CRC">Colones (CRC)</option>
              <option value="USD">Dólares (USD)</option>
              <option value="EUR">Euros (EUR)</option>
            </select>
          </div>
        </div>

        <h3>Receptor</h3>
        <div className="form-grid">
          <div className="form-row">
            <label htmlFor="receptor-tipo-id">Tipo de identificación</label>
            <select id="receptor-tipo-id" value={receptorTipoId} onChange={(e) => setReceptorTipoId(e.target.value)}>
              <option value="01">Física</option>
              <option value="02">Jurídica</option>
              <option value="03">DIMEX</option>
              <option value="04">NITE</option>
            </select>
          </div>
          <div className="form-row">
            <label htmlFor="receptor-identificacion">Número de identificación</label>
            <input
              id="receptor-identificacion"
              required
              value={receptorIdentificacion}
              onChange={(e) => setReceptorIdentificacion(e.target.value)}
            />
          </div>
          <div className="form-row">
            <label htmlFor="receptor-nombre">Nombre completo / razón social</label>
            <input
              id="receptor-nombre"
              required
              value={receptorNombre}
              onChange={(e) => setReceptorNombre(e.target.value)}
            />
          </div>
          <div className="form-row">
            <label htmlFor="receptor-correo">Correo electrónico</label>
            <input
              id="receptor-correo"
              type="email"
              value={receptorCorreo}
              onChange={(e) => setReceptorCorreo(e.target.value)}
            />
          </div>
        </div>

        <h3>Detalle</h3>
        {lineas.map((linea, idx) => (
          <div key={idx} className="linea-detalle">
            <div className="form-grid">
              <div className="form-row">
                <label>Código CABYS</label>
                <input
                  required
                  pattern="\d{13}"
                  title="El código CABYS debe tener 13 dígitos"
                  value={linea.codigoCabys}
                  onChange={(e) => actualizarLinea(idx, { codigoCabys: e.target.value })}
                />
              </div>
              <div className="form-row">
                <label>Detalle</label>
                <input
                  required
                  value={linea.detalle}
                  onChange={(e) => actualizarLinea(idx, { detalle: e.target.value })}
                />
              </div>
              <div className="form-row">
                <label>Cantidad</label>
                <input
                  type="number"
                  min={0.00001}
                  step="0.00001"
                  required
                  value={linea.cantidad}
                  onChange={(e) => actualizarLinea(idx, { cantidad: Number(e.target.value) })}
                />
              </div>
              <div className="form-row">
                <label>Precio unitario</label>
                <input
                  type="number"
                  min={0}
                  step="0.01"
                  required
                  value={linea.precioUnitario}
                  onChange={(e) => actualizarLinea(idx, { precioUnitario: Number(e.target.value) })}
                />
              </div>
              <div className="form-row">
                <label>Descuento</label>
                <input
                  type="number"
                  min={0}
                  step="0.01"
                  value={linea.montoDescuento ?? 0}
                  onChange={(e) => actualizarLinea(idx, { montoDescuento: Number(e.target.value) })}
                />
              </div>
              <div className="form-row">
                <label>IVA calculado</label>
                <input readOnly value={linea.impuestos[0]?.monto.toFixed(2) ?? "0.00"} />
              </div>
            </div>
            {lineas.length > 1 && (
              <button type="button" onClick={() => quitarLinea(idx)}>
                Quitar línea
              </button>
            )}
          </div>
        ))}

        <button type="button" onClick={agregarLinea}>
          + Agregar línea
        </button>

        <div className="card" style={{ marginTop: 16, background: "#f7f9fa" }}>
          <strong>Total estimado: {moneda} {totalEstimado.toLocaleString("es-CR", { minimumFractionDigits: 2 })}</strong>
        </div>

        {crearMutation.isError && (
          <p className="error-text">{(crearMutation.error as Error).message}</p>
        )}

        <button type="submit" disabled={!emisorActivo || crearMutation.isPending} style={{ marginTop: 16 }}>
          {crearMutation.isPending ? "Emitiendo…" : "Emitir comprobante"}
        </button>
      </form>
    </div>
  );
}
