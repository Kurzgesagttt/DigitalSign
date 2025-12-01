import { SignPdfForm } from './components/SignPdfForm';

function App() {
  return (
    <div className="app">
      <header className="navbar">
        <div className="navbar-brand">Kurzgesagttt</div>
      </header>
      <main className="main-content">
        <div className="content-wrapper">
          <h1 className="title">Assinador Digital de PDF</h1>
          <p className="subtitle">Assine seus documentos de forma segura e rápida</p>
          <SignPdfForm />
        </div>
      </main>
    </div>
  );
}

export default App;
