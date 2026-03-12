import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';

const API = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

interface User {
  id: number;
  nome: string;
  email: string;
  cpf: string;
}

interface Props {
  onRegister: (user: User) => void;
}

export const RegisterPage: React.FC<Props> = ({ onRegister }) => {
  const [form, setForm] = useState({
    nomeCompleto: '',
    email: '',
    telefone: '',
    cpf: '',
    dataNascimento: '',
    senha: '',
  });
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (field: string, value: string) => {
    setForm(prev => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const res = await axios.post(`${API}/v1/auth/register`, {
        ...form,
        cpf: form.cpf.replace(/\D/g, ''),
        telefone: form.telefone.replace(/\D/g, ''),
      });
      onRegister({ id: res.data.id, nome: res.data.nome, email: res.data.email, cpf: form.cpf.replace(/\D/g, '') });
      navigate('/');
    } catch (err: any) {
      const msg = err?.response?.data?.erro;
      setError(msg || 'Erro ao cadastrar.');
    } finally {
      setLoading(false);
    }
  };

  const inputStyle = {
    padding: '0.75rem 1rem',
    backgroundColor: 'var(--bg-tertiary)',
    border: '1px solid var(--border)',
    borderRadius: '8px',
    color: 'var(--text-primary)',
    fontSize: '1rem',
    fontFamily: 'inherit',
  };

  return (
    <div className="content-wrapper">
      <h1 className="title">Cadastro</h1>
      <p className="subtitle">Crie sua conta para assinar documentos</p>
      <form onSubmit={handleSubmit}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <input
            type="text"
            placeholder="Nome completo"
            value={form.nomeCompleto}
            onChange={(e) => handleChange('nomeCompleto', e.target.value)}
            required
            style={inputStyle}
          />
          <input
            type="email"
            placeholder="Email"
            value={form.email}
            onChange={(e) => handleChange('email', e.target.value)}
            required
            style={inputStyle}
          />
          <input
            type="tel"
            placeholder="Telefone (ex: 11999998888)"
            value={form.telefone}
            onChange={(e) => handleChange('telefone', e.target.value.replace(/\D/g, ''))}
            maxLength={11}
            required
            style={inputStyle}
          />
          <input
            type="text"
            placeholder="CPF (apenas números)"
            value={form.cpf}
            onChange={(e) => handleChange('cpf', e.target.value.replace(/\D/g, ''))}
            maxLength={11}
            required
            style={inputStyle}
          />
          <input
            type="date"
            placeholder="Data de nascimento"
            value={form.dataNascimento}
            onChange={(e) => handleChange('dataNascimento', e.target.value)}
            required
            style={inputStyle}
          />
          <input
            type="password"
            placeholder="Senha"
            value={form.senha}
            onChange={(e) => handleChange('senha', e.target.value)}
            required
            style={inputStyle}
          />
        </div>
        <button type="submit" disabled={loading}>
          {loading ? 'Cadastrando...' : 'Cadastrar'}
        </button>
        {error && <div className="error">{error}</div>}
        <p style={{ textAlign: 'center', color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: '1rem' }}>
          Já tem conta? <Link to="/login" style={{ color: 'var(--accent)' }}>Faça login</Link>
        </p>
      </form>
    </div>
  );
};
