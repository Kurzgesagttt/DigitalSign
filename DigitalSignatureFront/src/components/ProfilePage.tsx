import { useEffect, useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { api, getApiErrorMessage, type User } from '../api';

interface Profile {
  id: number;
  nomeCompleto: string;
  email: string;
  telefone: string | null;
  cpf: string | null;
  dataNascimento: string | null;
  role: string;
  criadoEm: string;
  totalAssinaturas: number;
  categoriasDados: { categoria: string; campos: string[]; finalidade: string }[];
  exclusao: { descricao: string; limitacoes: string[] };
}

export function ProfilePage({ user, onDeleted }: { user: User | null; onDeleted: () => void }) {
  const [profile, setProfile] = useState<Profile | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [attempt, setAttempt] = useState(0);
  const [senha, setSenha] = useState('');
  const [confirmed, setConfirmed] = useState(false);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    if (!user) return;
    const controller = new AbortController();
    setLoading(true);
    setError('');
    api.get<Profile>('/v1/users/me', { signal: controller.signal })
      .then(({ data }) => { if (!controller.signal.aborted) setProfile(data); })
      .catch((err: unknown) => { if (!controller.signal.aborted) setError(getApiErrorMessage(err, 'Não foi possível carregar seu perfil.')); })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [user, attempt]);

  async function deleteAccount(event: FormEvent) {
    event.preventDefault();
    if (!confirmed || !senha || deleting) return;
    setDeleting(true);
    setError('');
    try {
      await api.delete('/v1/users/me', { data: { senha } });
      setSenha('');
      onDeleted();
    } catch (err: unknown) {
      setSenha('');
      setError(getApiErrorMessage(err, 'Não foi possível excluir a conta. Confira sua senha e tente novamente.'));
    } finally {
      setDeleting(false);
    }
  }

  if (!user) return <section className="content-wrapper"><h1 className="title">Meu perfil</h1><p className="subtitle">Entre na sua conta para consultar seus dados.</p><Link className="button-link" to="/login">Entrar</Link></section>;

  return <div className="profile-layout">
    <header><p className="eyebrow">SUA CONTA</p><h1>Meu perfil e meus dados</h1><p className="muted">Consulte as informações vinculadas à sua conta e como elas são utilizadas.</p></header>
    {error && <p className="error" role="alert">{error}</p>}
    {loading ? <p role="status">Carregando seus dados…</p> : !profile ? <button onClick={() => setAttempt(value => value + 1)}>Tentar novamente</button> : <>
      <section className="profile-card" aria-labelledby="account-title">
        <h2 id="account-title">Informações da conta</h2>
        <dl className="profile-details">
          <div><dt>Nome completo</dt><dd>{profile.nomeCompleto}</dd></div>
          <div><dt>E-mail</dt><dd>{profile.email}</dd></div>
          <div><dt>Telefone</dt><dd>{profile.telefone || 'Não informado'}</dd></div>
          <div><dt>CPF</dt><dd>{profile.cpf || 'Não informado'}</dd></div>
          <div><dt>Data de nascimento</dt><dd>{profile.dataNascimento || 'Não informada'}</dd></div>
          <div><dt>Conta criada em</dt><dd>{profile.criadoEm ? new Date(profile.criadoEm).toLocaleDateString('pt-BR') : 'Não informada'}</dd></div>
          <div><dt>Perfil de acesso</dt><dd>{profile.role === 'ADMIN' ? 'Administrador' : 'Usuário'}</dd></div>
          <div><dt>Assinaturas registradas</dt><dd>{profile.totalAssinaturas}</dd></div>
        </dl>
      </section>
      <section className="profile-card" aria-labelledby="usage-title">
        <h2 id="usage-title">Como seus dados são utilizados</h2>
        <div className="data-usage">{profile.categoriasDados.map(category => <article key={category.categoria}><h3>{category.categoria}</h3><p>{category.finalidade}</p><p className="muted">Dados: {category.campos.join(', ')}.</p></article>)}</div>
      </section>
      <section className="profile-card danger-zone" aria-labelledby="delete-title">
        <h2 id="delete-title">Excluir minha conta</h2>
        <p>{profile.exclusao.descricao}</p>
        <p>A exclusão é permanente. Os registros de assinatura serão removidos e os links de verificação deixarão de funcionar.</p>
        <ul>{profile.exclusao.limitacoes.map(limitation => <li key={limitation}>{limitation}</li>)}</ul>
        <form onSubmit={deleteAccount}>
          <label htmlFor="delete-password">Confirme sua senha atual</label>
          <input id="delete-password" type="password" autoComplete="current-password" value={senha} onChange={event => setSenha(event.target.value)} required disabled={deleting} />
          <label className="confirmation"><input type="checkbox" checked={confirmed} onChange={event => setConfirmed(event.target.checked)} disabled={deleting} />Entendo as consequências e quero excluir permanentemente minha conta.</label>
          <button className="danger-button" disabled={!confirmed || !senha || deleting}>{deleting ? 'Excluindo…' : 'Excluir conta permanentemente'}</button>
        </form>
      </section>
    </>}
  </div>;
}
