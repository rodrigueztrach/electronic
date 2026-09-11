import axios from "axios";

/**
 * Cliente HTTP central de la aplicación. Todas las llamadas van contra la
 * API propia (backend Java), nunca directamente contra el API de Hacienda.
 */
export const httpClient = axios.create({
  baseURL: "/api/v1",
  headers: {
    "Content-Type": "application/json",
  },
});

// Adjunta el token de acceso (obtenido vía OAuth2/OIDC) a cada solicitud.
httpClient.interceptors.request.use((config) => {
  const token = window.localStorage.getItem("fe_access_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Manejo centralizado de errores de la API (ver ErrorResponse del backend).
httpClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      window.localStorage.removeItem("fe_access_token");
      window.localStorage.removeItem("fe_usuario");
      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }
    const mensaje =
      error.response?.data?.message ??
      error.message ??
      "Error de comunicación con el servidor";
    return Promise.reject(new Error(mensaje));
  }
);