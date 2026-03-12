import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';

const API = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

interface VerifyData {
  valido: boolean;
  nome?: string;
  email?: string;
  cpf?: string;
  assinadoEm?: string;
  erro?: string;
}

export const VerifyPage: React.FC = () => {
  const { code } = useParams<{ code: string }>();
  const [data, setData] = useState<VerifyData | null>(null);
  const [loading, setLoading] = useState(true);

  // Formulário de confirmação
  const [cpf, setCpf] = useState('');
  const [email, setEmail] = useState('');
  const [confirmResult, setConfirmResult] = useState<{ confirmado: boolean; mensagem?: string; erro?: string; nome?: string; assinadoEm?: string } | null>(null);
  const [confirming, setConfirming] = useState(false);

  useEffect(() => {
    if (!code) return;
    axios.get(`${API}/v1/verify/${code}`)
      .then(res => setData(res.data))
      .catch(() => setData({ valido: false, erro: 'Erro ao verificar assinatura.' }))
      .finally(() => setLoading(false));
  }, [code]);

  const handleConfirm = async (e: React.FormEvent) => {
    e.preventDefault();
    setConfirming(true);
    setConfirmResult(null);
    try {
      const res = await axios.post(`${API}/v1/verify/${code}`, {
        cpf: cpf.replace(/\D/g, ''),
        email: email.trim().toLowerCase(),
      });
      setConfirmResult(res.data);
    } catch {
      setConfirmResult({ confirmado: false, erro: 'Erro ao confirmar identidade.' });
    } finally {
      setConfirming(false);
    }
  };

  if (loading) {
    return (
      <div className="content-wrapper" style={{ textAlign: 'center' }}>
        <p style={{ color: 'var(--text-secondary)' }}>Verificando assinatura...</p>
      </div>
    );
  }

  if (!data || !data.valido) {
    return (
      <div className="content-wrapper" style={{ textAlign: 'center' }}>
        <h1 className="title" style={{ color: 'var(--error)' }}>Assinatura Não Encontrada</h1>
        <p className="subtitle">{data?.erro || 'O código de verificação é inválido.'}</p>
      </div>
    );
  }

  return (
    <div className="content-wrapper">
      <h1 className="title" style={{ color: 'var(--success)' }}>Assinatura Válida</h1>
      <p className="subtitle">Este documento foi assinado digitalmente</p>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', margin: '1.5rem 0', padding: '1rem', backgroundColor: 'var(--bg-tertiary)', borderRadius: '8px', border: '1px solid var(--border)' }}>
        <div><strong style={{ color: 'var(--text-secondary)' }}>Assinante:</strong> {data.nome}</div>
        <div><strong style={{ color: 'var(--text-secondary)' }}>Email:</strong> {data.email}</div>
        <div><strong style={{ color: 'var(--text-secondary)' }}>CPF:</strong> {data.cpf}</div>
        <div><strong style={{ color: 'var(--text-secondary)' }}>Data:</strong> {data.assinadoEm}</div>
      </div>

      <hr style={{ border: 'none', borderTop: '1px solid var(--border)', margin: '1.5rem 0' }} />

      <h2 style={{ fontSize: '1.2rem', color: 'var(--text-primary)', marginBottom: '0.5rem' }}>Confirmar Identidade do Assinante</h2>
      <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '1rem' }}>
        Insira o CPF e email do assinante para confirmar a identidade.
      </p>

      <form onSubmit={handleConfirm}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <input
            type="text"
            placeholder="CPF do assinante (apenas números)"
            value={cpf}
            onChange={(e) => setCpf(e.target.value.replace(/\D/g, ''))}
            maxLength={11}
            required
          />
          <input
            type="email"
            placeholder="Email do assinante"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>
        <button type="submit" disabled={confirming || !cpf || !email}>
          {confirming ? 'Verificando...' : 'Confirmar Identidade'}
        </button>
      </form>

      {confirmResult && (
        <div className={confirmResult.confirmado ? 'success' : 'error'} style={{ marginTop: '1rem' }}>
          {confirmResult.confirmado
            ? `Identidade confirmada! ${confirmResult.nome} assinou em ${confirmResult.assinadoEm}.`
            : (confirmResult.erro || 'CPF ou email não conferem.')}
        </div>
      )}
    </div>
  );
};
