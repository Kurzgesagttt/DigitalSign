import { describe, expect, it, beforeEach } from 'vitest';
import { api, loadStoredAuth, setAuthToken, storeUser, type User } from './api';

describe('api auth storage', () => {
  beforeEach(() => {
    localStorage.clear();
    setAuthToken(null);
  });

  it('stores and applies bearer token', () => {
    setAuthToken('abc123');

    expect(localStorage.getItem('digitalsign_token')).toBe('abc123');
    expect(api.defaults.headers.common.Authorization).toBe('Bearer abc123');
  });

  it('loads stored user and token', () => {
    const user: User = {
      id: 1,
      nome: 'Admin',
      email: 'admin@example.com',
      cpf: '12345678901',
      role: 'ADMIN',
    };

    storeUser(user);
    setAuthToken('token123');

    const stored = loadStoredAuth();

    expect(stored.user).toEqual(user);
    expect(stored.token).toBe('token123');
    expect(api.defaults.headers.common.Authorization).toBe('Bearer token123');
  });
});
