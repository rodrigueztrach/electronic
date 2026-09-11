import { NavLink, Route, Routes, useNavigate } from "react-router-dom";
import DashboardPage from "./pages/DashboardPage";
import ComprobantesListPage from "./pages/ComprobantesListPage";
import NuevoComprobantePage from "./pages/NuevoComprobantePage";
import ReceptoresPage from "./pages/ReceptoresPage";
import ProductosPage from "./pages/ProductosPage";
import EmisoresPage from "./pages/EmisoresPage";
import LoginPage from "./pages/LoginPage";
import RequireAuth from "./components/RequireAuth";
import { EmisorProvider } from "./context/EmisorContext";
import { authApi } from "@/api/authApi";

function AppShell() {
  const navigate = useNavigate();
  const usuario = authApi.usuarioActual();

  const cerrarSesion = () => {
    authApi.logout();
    navigate("/login");
  };

  return (
    <EmisorProvider>
      <div className="app-shell">
        <aside className="sidebar">
          <h1>Facturación Electrónica CR — v4.4</h1>
          <nav>
            <NavLink to="/" end>
              Panel
            </NavLink>
            <NavLink to="/comprobantes">Comprobantes</NavLink>
            <NavLink to="/comprobantes/nuevo">Nuevo comprobante</NavLink>
            <NavLink to="/receptores">Clientes</NavLink>
            <NavLink to="/productos">Productos</NavLink>
            <NavLink to="/emisores">Emisores</NavLink>
          </nav>

          <div style={{ marginTop: 32, borderTop: "1px solid rgba(255,255,255,0.2)", paddingTop: 16 }}>
            {usuario && (
              <p style={{ fontSize: 13, opacity: 0.85, marginBottom: 8 }}>
                {usuario.nombreCompleto}
                <br />
                {usuario.correo}
              </p>
            )}
            <button onClick={cerrarSesion} style={{ width: "100%" }}>
              Cerrar sesión
            </button>
          </div>
        </aside>

        <main className="content">
          <Routes>
            <Route path="/" element={<DashboardPage />} />
            <Route path="/comprobantes" element={<ComprobantesListPage />} />
            <Route path="/comprobantes/nuevo" element={<NuevoComprobantePage />} />
            <Route path="/receptores" element={<ReceptoresPage />} />
            <Route path="/productos" element={<ProductosPage />} />
            <Route path="/emisores" element={<EmisoresPage />} />
          </Routes>
        </main>
      </div>
    </EmisorProvider>
  );
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<RequireAuth />}>
        <Route path="/*" element={<AppShell />} />
      </Route>
    </Routes>
  );
}