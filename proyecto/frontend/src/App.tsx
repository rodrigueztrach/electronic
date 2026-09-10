import { NavLink, Route, Routes } from "react-router-dom";
import DashboardPage from "./pages/DashboardPage";
import ComprobantesListPage from "./pages/ComprobantesListPage";
import NuevoComprobantePage from "./pages/NuevoComprobantePage";
import ReceptoresPage from "./pages/ReceptoresPage";
import ProductosPage from "./pages/ProductosPage";
import EmisoresPage from "./pages/EmisoresPage";
import { EmisorProvider } from "./context/EmisorContext";

export default function App() {
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
