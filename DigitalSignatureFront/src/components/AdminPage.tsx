import React, { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import { api, getApiErrorMessage, type User } from '../api';

interface Props {
  user: User | null;
}

interface Metrics {
  totalUsuarios: number;
  totalAssinaturas: number;
  assinaturasValidas: number;
  assinaturasRevogadas: number;
  assinaturasSuspeitas: number;
  topAssinantes: Array<{ email: string; nome: string; total: number }>;
}

interface AdminUser {
  id: number;
  nome: string;
  email: string;
  cpf: string;
  role: string;
  bloqueado: boolean;
  bloqueadoMotivo?: string;
  criadoEm: string;
}

interface AdminSignature {
  id: number;
  verificationCode: string;
  usuarioId?: number;
  nome: string;
  email: string;
  cpf: string;
  status: string;
  assinadoEm: string;
  ipAddress?: string;
  originalPdfHash?: string;
  signedPdfHash?: string;
  revogacaoMotivo?: string;
}

interface AuditLog {
  id: number;
  eventType: string;
  result: string;
  actorEmail?: string;
  actorRole?: string;
  targetType?: string;
  targetId?: number;
  details?: string;
  occurredAt: string;
}

type Tab = 'metrics' | 'users' | 'signatures' | 'audit';

export const AdminPage: React.FC<Props> = ({ user }) => {
  const [tab, setTab] = useState<Tab>('metrics');
  const [metrics, setMetrics] = useState<Metrics | null>(null);
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [signatures, setSignatures] = useState<AdminSignature[]>([]);
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [signatureCode, setSignatureCode] = useState('');
  const [signatureStatus, setSignatureStatus] = useState('');

  const loadAll = async () => {
    setLoading(true);
    setError(null);
    try {
      const [metricsRes, usersRes, signaturesRes, auditRes] = await Promise.all([
        api.get('/v1/admin/metrics'),
        api.get('/v1/admin/users'),
        api.get('/v1/admin/signatures'),
        api.get('/v1/admin/audit-logs'),
      ]);
      setMetrics(metricsRes.data);
      setUsers(usersRes.data.content || []);
      setSignatures(signaturesRes.data.content || []);
      setAuditLogs(auditRes.data.content || []);
    } catch (err: unknown) {
      setError(getApiErrorMessage(err, 'Erro ao carregar painel admin.'));
    } finally {
      setLoading(false);
    }
  };

  const searchSignatures = async () => {
    setLoading(true);
    setError(null);
    try {
      const params: Record<string, string> = {};
      if (signatureCode.trim()) params.code = signatureCode.trim();
      if (signatureStatus) params.status = signatureStatus;
      const res = await api.get('/v1/admin/signatures', { params });
      setSignatures(res.data.content || []);
    } catch (err: unknown) {
      setError(getApiErrorMessage(err, 'Erro ao consultar assinaturas.'));
    } finally {
      setLoading(false);
    }
  };

  const blockUser = async (id: number) => {
    const motivo = window.prompt('Motivo do bloqueio');
    if (!motivo) return;
    await api.patch(`/v1/admin/users/${id}/block`, { motivo });
    await loadAll();
  };

  const unblockUser = async (id: number) => {
    await api.patch(`/v1/admin/users/${id}/unblock`);
    await loadAll();
  };

  const revokeSignature = async (id: number) => {
    const motivo = window.prompt('Motivo da revogacao');
    if (!motivo) return;
    await api.patch(`/v1/admin/signatures/${id}/revoke`, { motivo });
    await loadAll();
  };

  const downloadReport = async (path: string, filename: string) => {
    setError(null);
    try {
      const response = await api.get(path, { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const a = document.createElement('a');
      a.href = url;
      a.download = filename;
      a.click();
      window.URL.revokeObjectURL(url);
    } catch (err: unknown) {
      setError(getApiErrorMessage(err, 'Erro ao exportar relatorio.'));
    }
  };

  useEffect(() => {
    if (user?.role === 'ADMIN') {
      loadAll();
    }
  }, [user?.role]);

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (user.role !== 'ADMIN') {
    return <Navigate to="/" replace />;
  }

  return (
    <div className="admin-layout">
      <div className="admin-header">
        <div>
          <h1 className="title" style={{ textAlign: 'left', marginBottom: 0 }}>Painel Admin</h1>
          <p className="subtitle" style={{ textAlign: 'left', marginBottom: 0 }}>Usuarios, assinaturas e auditoria</p>
        </div>
        <button onClick={loadAll} disabled={loading}>{loading ? 'Atualizando...' : 'Atualizar'}</button>
      </div>

      <div className="admin-tabs">
        <button className={tab === 'metrics' ? 'active' : ''} onClick={() => setTab('metrics')}>Metricas</button>
        <button className={tab === 'users' ? 'active' : ''} onClick={() => setTab('users')}>Usuarios</button>
        <button className={tab === 'signatures' ? 'active' : ''} onClick={() => setTab('signatures')}>Assinaturas</button>
        <button className={tab === 'audit' ? 'active' : ''} onClick={() => setTab('audit')}>Auditoria</button>
      </div>

      {error && <div className="error">{error}</div>}

      {tab === 'metrics' && metrics && (
        <section className="admin-section">
          <div className="metric-grid">
            <Metric label="Usuarios" value={metrics.totalUsuarios} />
            <Metric label="Assinaturas" value={metrics.totalAssinaturas} />
            <Metric label="Validas" value={metrics.assinaturasValidas} />
            <Metric label="Revogadas" value={metrics.assinaturasRevogadas} />
            <Metric label="Suspeitas" value={metrics.assinaturasSuspeitas} />
          </div>
          <h2>Top assinantes</h2>
          <DataTable
            headers={['Nome', 'Email', 'Total']}
            rows={metrics.topAssinantes.map(item => [item.nome, item.email, item.total])}
          />
        </section>
      )}

      {tab === 'users' && (
        <section className="admin-section">
          <DataTable
            headers={['ID', 'Nome', 'Email', 'CPF', 'Role', 'Status', 'Criado em', 'Acoes']}
            rows={users.map(item => [
              item.id,
              item.nome,
              item.email,
              item.cpf,
              item.role,
              item.bloqueado ? `Bloqueado${item.bloqueadoMotivo ? `: ${item.bloqueadoMotivo}` : ''}` : 'Ativo',
              item.criadoEm,
              item.bloqueado
                ? <button onClick={() => unblockUser(item.id)}>Desbloquear</button>
                : <button onClick={() => blockUser(item.id)} disabled={item.id === user.id}>Bloquear</button>,
            ])}
          />
        </section>
      )}

      {tab === 'signatures' && (
        <section className="admin-section">
          <div className="filter-row">
            <input value={signatureCode} onChange={(e) => setSignatureCode(e.target.value)} placeholder="Codigo de verificacao" />
            <select value={signatureStatus} onChange={(e) => setSignatureStatus(e.target.value)}>
              <option value="">Todos os status</option>
              <option value="VALIDA">VALIDA</option>
              <option value="REVOGADA">REVOGADA</option>
              <option value="SUSPEITA">SUSPEITA</option>
            </select>
            <button onClick={searchSignatures}>Filtrar</button>
            <button onClick={() => downloadReport('/v1/admin/reports/signatures.csv', 'assinaturas.csv')}>Exportar CSV</button>
          </div>
          <DataTable
            headers={['Codigo', 'Nome', 'Email', 'CPF', 'Status', 'Data', 'IP', 'Hash original', 'Acoes']}
            rows={signatures.map(item => [
              item.verificationCode,
              item.nome,
              item.email,
              item.cpf,
              item.status,
              item.assinadoEm,
              item.ipAddress || '-',
              shortHash(item.originalPdfHash),
              item.status === 'REVOGADA'
                ? (item.revogacaoMotivo || 'Revogada')
                : <button onClick={() => revokeSignature(item.id)}>Revogar</button>,
            ])}
          />
        </section>
      )}

      {tab === 'audit' && (
        <section className="admin-section">
          <div className="filter-row">
            <button onClick={() => downloadReport('/v1/admin/reports/audit-logs.csv', 'auditoria.csv')}>Exportar CSV</button>
          </div>
          <DataTable
            headers={['Data', 'Evento', 'Resultado', 'Ator', 'Alvo', 'Detalhes']}
            rows={auditLogs.map(item => [
              item.occurredAt,
              item.eventType,
              item.result,
              item.actorEmail || '-',
              `${item.targetType || '-'} ${item.targetId || ''}`.trim(),
              item.details || '-',
            ])}
          />
        </section>
      )}
    </div>
  );
};

const Metric: React.FC<{ label: string; value: number }> = ({ label, value }) => (
  <div className="metric-item">
    <span>{label}</span>
    <strong>{value}</strong>
  </div>
);

const DataTable: React.FC<{ headers: string[]; rows: Array<Array<React.ReactNode>> }> = ({ headers, rows }) => (
  <div className="table-wrap">
    <table>
      <thead>
        <tr>{headers.map(header => <th key={header}>{header}</th>)}</tr>
      </thead>
      <tbody>
        {rows.length === 0 ? (
          <tr><td colSpan={headers.length}>Nenhum registro encontrado.</td></tr>
        ) : rows.map((row, rowIndex) => (
          <tr key={rowIndex}>
            {row.map((cell, cellIndex) => <td key={cellIndex}>{cell}</td>)}
          </tr>
        ))}
      </tbody>
    </table>
  </div>
);

function shortHash(hash?: string) {
  if (!hash) return '-';
  return `${hash.slice(0, 10)}...`;
}
