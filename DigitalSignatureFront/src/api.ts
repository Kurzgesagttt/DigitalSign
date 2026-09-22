import axios from 'axios';

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

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

export function setAuthToken(token: string | null) {
  if (token) {
    api.defaults.headers.common.Authorization = `Bearer ${token}`;
    return;
  }

  delete api.defaults.headers.common.Authorization;
}

export function getApiErrorMessage(error: unknown, fallback: string) {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { erro?: string } | undefined;
    return data?.erro || fallback;
  }
  return fallback;
}
