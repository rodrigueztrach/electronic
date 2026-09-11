import { Navigate, Outlet } from "react-router-dom";
import { authApi } from "@/api/authApi";

/**
 * Envuelve las rutas privadas. Esta comprobación es solo de conveniencia en
 * el cliente (existencia del token en localStorage) — la seguridad real la
 * impone siempre el backend, que rechaza con 401 cualquier request sin un
 * JWT válido.
 */
export default function RequireAuth() {
  if (!authApi.estaAutenticado()) {
    return <Navigate to="/login" replace />;
  }
  return <Outlet />;
}