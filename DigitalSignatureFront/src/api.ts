import axios from 'axios';

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export interface User {
  id: number;
  nome: string;
  email: string;
  cpf: string;
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
    localStorage.setItem('digitalsign_token', token);
    return;
  }

  delete api.defaults.headers.common.Authorization;
  localStorage.removeItem('digitalsign_token');
}

export function storeUser(user: User | null) {
  if (user) {
    localStorage.setItem('digitalsign_user', JSON.stringify(user));
    return;
  }

  localStorage.removeItem('digitalsign_user');
}

export function loadStoredAuth(): { user: User | null; token: string | null } {
  const token = localStorage.getItem('digitalsign_token');
  const rawUser = localStorage.getItem('digitalsign_user');
  let user: User | null = null;

  if (rawUser) {
    try {
      user = JSON.parse(rawUser);
    } catch {
      localStorage.removeItem('digitalsign_user');
    }
  }

  if (token) {
    setAuthToken(token);
  }

  return { user, token };
}

export function getApiErrorMessage(error: unknown, fallback: string) {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { erro?: string } | undefined;
    return data?.erro || fallback;
  }
  return fallback;
}
