import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { authApi } from "@/api/authApi";

export default function LoginPage() {
  const navigate = useNavigate();
  const [modo, setModo] = useState<"login" | "registro">("login");

  const [correo, setCorreo] = useState("");
  const [password, setPassword] = useState("");
  const [nombreCompleto, setNombreCompleto] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [cargando, setCargando] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setCargando(true);
    try {
      if (modo === "login") {
        await authApi.login({ correo, password });
      } else {
        await authApi.registrar({ correo, password, nombreCompleto });
      }
      navigate("/");
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setCargando(false);
    }
  };

  return (
    <div
      style={{
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        background: "#f7f9fa",
      }}
    >
      <form onSubmit={handleSubmit} className="card" style={{ width: 360 }}>
        <h2 style={{ marginTop: 0 }}>Facturación Electrónica CR</h2>
        <p style={{ color: "#666", fontSize: 14, marginTop: -8 }}>
          {modo === "login" ? "Inicia sesión para continuar" : "Crea tu cuenta de acceso"}
        </p>

        {modo === "registro" && (
          <div className="form-row">
            <label>Nombre completo</label>
            <input required value={nombreCompleto} onChange={(e) => setNombreCompleto(e.target.value)} />
          </div>
        )}

        <div className="form-row">
          <label>Correo electrónico</label>
          <input
            type="email"
            required
            value={correo}
            onChange={(e) => setCorreo(e.target.value)}
            autoComplete="username"
          />
        </div>

        <div className="form-row">
          <label>Contraseña</label>
          <input
            type="password"
            required
            minLength={modo === "registro" ? 8 : undefined}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete={modo === "login" ? "current-password" : "new-password"}
          />
        </div>

        {error && <p className="error-text">{error}</p>}

        <button type="submit" disabled={cargando} style={{ width: "100%", marginTop: 8 }}>
          {cargando ? "Un momento…" : modo === "login" ? "Iniciar sesión" : "Crear cuenta"}
        </button>

        <p style={{ fontSize: 13, textAlign: "center", marginTop: 16 }}>
          {modo === "login" ? (
            <>
              ¿No tienes cuenta?{" "}
              <a href="#" onClick={(e) => { e.preventDefault(); setModo("registro"); setError(null); }}>
                Regístrate
              </a>
            </>
          ) : (
            <>
              ¿Ya tienes cuenta?{" "}
              <a href="#" onClick={(e) => { e.preventDefault(); setModo("login"); setError(null); }}>
                Inicia sesión
              </a>
            </>
          )}
        </p>
      </form>
    </div>
  );
}