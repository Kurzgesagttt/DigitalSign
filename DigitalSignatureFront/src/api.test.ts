import { describe, expect, it, beforeEach } from 'vitest';
import { api, setAuthToken } from './api';

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
});
