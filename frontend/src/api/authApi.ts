import { httpClient } from "./httpClient";

export interface LoginInput {
  correo: string;
  password: string;
}

export interface RegistroInput extends LoginInput {
  nombreCompleto: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  correo: string;
  nombreCompleto: string;
}

const TOKEN_KEY = "fe_access_token";
const USUARIO_KEY = "fe_usuario";

export const authApi = {
  login: async (input: LoginInput): Promise<LoginResponse> => {
    const { data } = await httpClient.post<LoginResponse>("/auth/login", input);
    guardarSesion(data);
    return data;
  },

  registrar: async (input: RegistroInput): Promise<LoginResponse> => {
    const { data } = await httpClient.post<LoginResponse>("/auth/registro", input);
    guardarSesion(data);
    return data;
  },

  logout: (): void => {
    window.localStorage.removeItem(TOKEN_KEY);
    window.localStorage.removeItem(USUARIO_KEY);
  },

  estaAutenticado: (): boolean => {
    return !!window.localStorage.getItem(TOKEN_KEY);
  },

  usuarioActual: (): { correo: string; nombreCompleto: string } | null => {
    const raw = window.localStorage.getItem(USUARIO_KEY);
    return raw ? JSON.parse(raw) : null;
  },
};

function guardarSesion(data: LoginResponse): void {
  window.localStorage.setItem(TOKEN_KEY, data.accessToken);
  window.localStorage.setItem(
    USUARIO_KEY,
    JSON.stringify({ correo: data.correo, nombreCompleto: data.nombreCompleto })
  );
}