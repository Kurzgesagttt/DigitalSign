import { Routes, Route, Link, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { SignPdfForm } from './components/SignPdfForm';
import { LoginPage } from './components/LoginPage';
import { RegisterPage } from './components/RegisterPage';
import { VerifyPage } from './components/VerifyPage';
import { AdminPage } from './components/AdminPage';
import { ProfilePage } from './components/ProfilePage';
import { setAuthToken, type AuthPayload, type User } from './api';

function App() {
  const [user, setUser] = useState<User | null>(null);
  const navigate = useNavigate();

  const handleAuth = (auth: AuthPayload) => {
    const nextUser: User = {
      id: auth.id,
      nome: auth.nome,
      email: auth.email,
      role: auth.role,
    };
    setUser(nextUser);
    setAuthToken(auth.token);
  };

  const handleLogout = () => {
    setUser(null);
    setAuthToken(null);
    navigate('/login');
  };

  return (
    <div className="app">
      <header className="navbar">
        <Link to="/" className="navbar-brand">Digital Sign <span>- PIC</span></Link>
        <nav style={{ display: 'flex', gap: '1rem', alignItems: 'center', flexWrap: 'wrap', justifyContent: 'flex-end' }}>
          {user ? (
            <>
              <span style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Ola, {user.nome.split(' ')[0]}</span>
              <Link to="/" style={{ color: 'var(--accent)', textDecoration: 'none', fontSize: '0.9rem' }}>Assinar PDF</Link>
              <Link to="/perfil" className="nav-link">Meu perfil</Link>
              {user.role === 'ADMIN' && (
                <Link to="/admin" style={{ color: 'var(--accent)', textDecoration: 'none', fontSize: '0.9rem' }}>Admin</Link>
              )}
              <button onClick={handleLogout} style={{ background: 'none', border: '1px solid var(--border)', color: 'var(--text-secondary)', padding: '0.4rem 0.8rem', borderRadius: '6px', cursor: 'pointer', fontSize: '0.85rem', textTransform: 'none', letterSpacing: 'normal' }}>Sair</button>
            </>
          ) : (
            <>
              <Link to="/login" style={{ color: 'var(--accent)', textDecoration: 'none', fontSize: '0.9rem' }}>Login</Link>
              <Link to="/cadastro" style={{ color: 'var(--text-secondary)', textDecoration: 'none', fontSize: '0.9rem' }}>Cadastro</Link>
            </>
          )}
        </nav>
      </header>
      <main className="main-content">
        <Routes>
          <Route path="/login" element={<LoginPage onLogin={handleAuth} />} />
          <Route path="/cadastro" element={<RegisterPage onRegister={handleAuth} />} />
          <Route path="/verificar/:code" element={<VerifyPage />} />
          <Route path="/admin" element={<AdminPage user={user} />} />
          <Route path="/perfil" element={<ProfilePage user={user} onDeleted={handleLogout} />} />
          <Route path="/" element={
            user ? (
              <div className="content-wrapper">
                <h1 className="title">Assinador Digital de PDF</h1>
                <p className="subtitle">Assine seus documentos de forma segura e rapida</p>
                <SignPdfForm user={user} />
              </div>
            ) : (
              <div className="landing">
                <section className="hero">
                  <p className="eyebrow">DIGITAL SIGN · PROJETO PIC</p>
                  <h1>Seu documento.<br /><span>Sua assinatura.</span></h1>
                  <p className="hero-description">Assine PDFs e consulte os registros de assinatura em um só lugar. Simples, do envio à consulta.</p>
                  <div className="hero-actions">
                    <Link to="/cadastro" className="button-link">Criar minha conta <span aria-hidden="true">↗</span></Link>
                    <Link to="/login" className="button-link secondary">Entrar</Link>
                  </div>
                  <p className="hero-note">Já recebeu um documento? Use o link de verificação presente no PDF.</p>
                </section>
                <section className="workflow" aria-label="Como funciona">
                  <p className="eyebrow">DO ARQUIVO À ASSINATURA</p>
                  <ol>
                    <li><span>01</span><div><h2>Envie seu PDF</h2><p>Escolha o documento que deseja assinar.</p></div></li>
                    <li><span>02</span><div><h2>Assine com sua conta</h2><p>Vincule o registro de assinatura à sua conta.</p></div></li>
                    <li><span>03</span><div><h2>Compartilhe e consulte</h2><p>Baixe o PDF e consulte seu registro de assinatura.</p></div></li>
                  </ol>
                </section>
                </div>
            )
          } />
        </Routes>
      </main>
      <footer className="site-footer"><span>Digital Sign - PIC</span><span>Assinatura e verificação de documentos PDF</span></footer>
    </div>
  );
}

export default App;
