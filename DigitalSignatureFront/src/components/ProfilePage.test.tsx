import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { ProfilePage } from './ProfilePage';
import { api, type User } from '../api';

const user: User = { id: 42, nome: 'Pessoa Teste', email: 'pessoa@example.com', role: 'USER' };
const profile = {
  ...user, nomeCompleto: user.nome, telefone: null, cpf: null, dataNascimento: null,
  criadoEm: '2026-01-01T12:00:00', totalAssinaturas: 7,
  categoriasDados: [{ categoria: 'Cadastro', campos: ['Nome', 'E-mail'], finalidade: 'Identificar sua conta.' }],
  exclusao: { descricao: 'Remove seus dados de cadastro.', limitacoes: ['PDFs baixados não são removidos.'] },
};

describe('ProfilePage', () => {
  afterEach(() => vi.restoreAllMocks());

  it('requires password and explicit confirmation, clearing the session only after deletion succeeds', async () => {
    const actor = userEvent.setup();
    vi.spyOn(api, 'get').mockResolvedValue({ data: profile });
    const remove = vi.spyOn(api, 'delete').mockResolvedValue({ status: 204 });
    const onDeleted = vi.fn();
    render(<MemoryRouter><ProfilePage user={user} onDeleted={onDeleted} /></MemoryRouter>);
    expect(await screen.findByText('pessoa@example.com')).toBeInTheDocument();
    expect(screen.getByText('7')).toBeInTheDocument();
    expect(screen.getByText('Identificar sua conta.')).toBeInTheDocument();
    const submit = screen.getByRole('button', { name: 'Excluir conta permanentemente' });
    expect(submit).toBeDisabled();
    await actor.type(screen.getByLabelText('Confirme sua senha atual'), 'synthetic-password');
    expect(submit).toBeDisabled();
    await actor.click(screen.getByRole('checkbox'));
    await actor.click(submit);
    await waitFor(() => expect(onDeleted).toHaveBeenCalledOnce());
    expect(remove).toHaveBeenCalledWith('/v1/users/me', { data: { senha: 'synthetic-password' } });
  });

  it('keeps the session and clears the entered password after deletion fails', async () => {
    const actor = userEvent.setup();
    vi.spyOn(api, 'get').mockResolvedValue({ data: profile });
    vi.spyOn(api, 'delete').mockRejectedValue(new Error('failed'));
    const onDeleted = vi.fn();
    render(<MemoryRouter><ProfilePage user={user} onDeleted={onDeleted} /></MemoryRouter>);
    const password = await screen.findByLabelText('Confirme sua senha atual');
    await actor.type(password, 'wrong-password');
    await actor.click(screen.getByRole('checkbox'));
    await actor.click(screen.getByRole('button', { name: 'Excluir conta permanentemente' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('Não foi possível excluir a conta');
    expect(password).toHaveValue('');
    expect(onDeleted).not.toHaveBeenCalled();
  });
});
