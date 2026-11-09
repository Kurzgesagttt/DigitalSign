import { AxiosError } from 'axios';
import { describe, expect, it, beforeEach } from 'vitest';
import { api, getApiErrorMessage, setAuthToken } from './api';

describe('api auth storage', () => {
  beforeEach(() => {
    setAuthToken(null);
  });

  it('stores and applies bearer token', () => {
    setAuthToken('abc123');

    expect(api.defaults.headers.common.Authorization).toBe('Bearer abc123');
  });

  it('clears the bearer token from memory', () => {
    setAuthToken('token123');
    setAuthToken(null);

    expect(api.defaults.headers.common.Authorization).toBeUndefined();
  });

  it('shows a useful message when the API cannot be reached', () => {
    const networkError = new AxiosError('Network Error', 'ERR_NETWORK');

    expect(getApiErrorMessage(networkError, 'Fallback')).toBe(
      'Não foi possível conectar ao serviço. Verifique sua conexão e tente novamente.'
    );
  });

  it('uses a safe API error returned by the server', () => {
    const serverError = new AxiosError('Request failed', 'ERR_BAD_REQUEST', undefined, undefined, {
      status: 400,
      statusText: 'Bad Request',
      headers: {},
      config: { headers: {} },
      data: { erro: 'Requisição inválida.' },
    });

    expect(getApiErrorMessage(serverError, 'Fallback')).toBe('Requisição inválida.');
  });
});
