import { Routes, Route, Link, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { SignPdfForm } from './components/SignPdfForm';
import { LoginPage } from './components/LoginPage';
import { RegisterPage } from './components/RegisterPage';
import { VerifyPage } from './components/VerifyPage';
import { AdminPage } from './components/AdminPage';
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
        <Link to="/" className="navbar-brand" style={{ textDecoration: 'none', color: 'inherit' }}>Kurzgesagttt</Link>
        <nav style={{ display: 'flex', gap: '1rem', alignItems: 'center', flexWrap: 'wrap', justifyContent: 'flex-end' }}>
          {user ? (
            <>
              <span style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Ola, {user.nome.split(' ')[0]}</span>
              <Link to="/" style={{ color: 'var(--accent)', textDecoration: 'none', fontSize: '0.9rem' }}>Assinar PDF</Link>
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
          <Route path="/" element={
            user ? (
              <div className="content-wrapper">
                <h1 className="title">Assinador Digital de PDF</h1>
                <p className="subtitle">Assine seus documentos de forma segura e rapida</p>
                <SignPdfForm user={user} />
              </div>
            ) : (
              <div className="content-wrapper" style={{ textAlign: 'center' }}>
                <h1 className="title">Assinador Digital de PDF</h1>
                <p className="subtitle">Faca login ou cadastre-se para assinar seus documentos</p>
                <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center', marginTop: '2rem' }}>
                  <Link to="/login"><button>Login</button></Link>
                  <Link to="/cadastro"><button style={{ backgroundColor: 'transparent', border: '1px solid var(--accent)', color: 'var(--accent)' }}>Cadastro</button></Link>
                </div>
              </div>
            )
          } />
        </Routes>
      </main>
    </div>
  );
}

export default App;
