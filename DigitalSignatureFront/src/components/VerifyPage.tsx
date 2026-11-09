import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { api } from '../api';

interface VerifyData {
  valido: boolean;
  assinadoEm?: string;
  status?: string;
  erro?: string;
}

export const VerifyPage: React.FC = () => {
  const { code } = useParams<{ code: string }>();
  const [data, setData] = useState<VerifyData | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!code) return;
    api.get(`/v1/verify/${code}`)
      .then(res => setData(res.data))
      .catch(() => setData({ valido: false, erro: 'Erro ao verificar assinatura.' }))
      .finally(() => setLoading(false));
  }, [code]);

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
        <div><strong style={{ color: 'var(--text-secondary)' }}>Data:</strong> {data.assinadoEm}</div>
        <div><strong style={{ color: 'var(--text-secondary)' }}>Status:</strong> {data.status}</div>
      </div>
      <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
        A consulta pública confirma apenas o estado do registro e não divulga dados do assinante.
      </p>
    </div>
  );
};
