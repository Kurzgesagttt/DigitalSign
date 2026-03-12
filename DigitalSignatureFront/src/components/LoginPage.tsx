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
  onLogin: (user: User) => void;
}

export const LoginPage: React.FC<Props> = ({ onLogin }) => {
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const res = await axios.post(`${API}/v1/auth/login`, { email, senha });
      onLogin({ id: res.data.id, nome: res.data.nome, email: res.data.email, cpf: res.data.cpf });
      navigate('/');
    } catch (err: any) {
      const msg = err?.response?.data?.erro;
      setError(msg || 'Erro ao fazer login.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="content-wrapper">
      <h1 className="title">Login</h1>
      <p className="subtitle">Entre com suas credenciais</p>
      <form onSubmit={handleSubmit}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <input
            type="email"
            placeholder="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
          <input
            type="password"
            placeholder="Senha"
            value={senha}
            onChange={(e) => setSenha(e.target.value)}
            required
          />
        </div>
        <button type="submit" disabled={loading || !email || !senha}>
          {loading ? 'Entrando...' : 'Entrar'}
        </button>
        {error && <div className="error">{error}</div>}
        <p style={{ textAlign: 'center', color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: '1rem' }}>
          Não tem conta? <Link to="/cadastro" style={{ color: 'var(--accent)' }}>Cadastre-se</Link>
        </p>
      </form>
    </div>
  );
};
