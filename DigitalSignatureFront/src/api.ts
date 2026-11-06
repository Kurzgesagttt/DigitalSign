import axios from 'axios';

const configuredApiBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim();
const apiNotConfiguredMessage = 'API_NOT_CONFIGURED';

export const API_BASE_URL = configuredApiBaseUrl || (import.meta.env.DEV ? 'http://localhost:8080' : undefined);

export interface User {
  id: number;
  nome: string;
  email: string;
  role: 'USER' | 'ADMIN';
}

export interface AuthPayload extends User {
  token: string;
}

export const api = axios.create({
  baseURL: API_BASE_URL,
});

api.interceptors.request.use((config) => {
  if (!API_BASE_URL) {
    return Promise.reject(new Error(apiNotConfiguredMessage));
  }
  return config;
});

export function setAuthToken(token: string | null) {
  if (token) {
    api.defaults.headers.common.Authorization = `Bearer ${token}`;
    return;
  }

  delete api.defaults.headers.common.Authorization;
}

export function getApiErrorMessage(error: unknown, fallback: string) {
  if (error instanceof Error && error.message === apiNotConfiguredMessage) {
    return 'O serviço ainda não está configurado para este ambiente. Tente novamente mais tarde.';
  }

  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { erro?: string } | undefined;
    if (data?.erro) return data.erro;
    if (!error.response) return 'Não foi possível conectar ao serviço. Verifique sua conexão e tente novamente.';
  }
  return fallback;
}
