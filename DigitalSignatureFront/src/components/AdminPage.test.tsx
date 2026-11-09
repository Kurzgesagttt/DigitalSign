import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { AdminPage } from './AdminPage';
import { api, type User } from '../api';

const admin: User = {
  id: 1,
  nome: 'Admin Teste',
  email: 'admin@example.com',
  cpf: '12345678901',
  role: 'ADMIN',
};

describe('AdminPage', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('loads metrics for admin users', async () => {
    vi.spyOn(api, 'get').mockImplementation((url: string) => {
      if (url === '/v1/admin/metrics') {
        return Promise.resolve({
          data: {
            totalUsuarios: 2,
            totalAssinaturas: 3,
            assinaturasValidas: 2,
            assinaturasRevogadas: 1,
            assinaturasSuspeitas: 0,
            topAssinantes: [{ nome: 'Assinante', email: 'signer@example.com', total: 3 }],
          },
        });
      }
      return Promise.resolve({ data: { content: [] } });
    });

    render(
      <MemoryRouter>
        <AdminPage user={admin} />
      </MemoryRouter>
    );

    await waitFor(() => expect(screen.getByText('Usuarios')).toBeInTheDocument());
    expect(screen.getAllByText('3').length).toBeGreaterThan(0);
    expect(screen.getByText('signer@example.com')).toBeInTheDocument();
  });
});
