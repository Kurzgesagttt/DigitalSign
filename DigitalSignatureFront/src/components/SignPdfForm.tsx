import React, { useState } from 'react';
import type { ChangeEvent, FormEvent } from 'react';

import { useSignPdf } from '../hooks/useSignPdf';

export const SignPdfForm: React.FC = () => {
  const [file, setFile] = useState<File | null>(null);
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [cpf, setCpf] = useState('');
  const { signPdf, loading, error } = useSignPdf();

  const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
    const selectedFile = e.target.files?.[0] || null;
    setFile(selectedFile);
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (file && nome && email && cpf) {
      await signPdf({ file, nome, email, cpf });
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <div className="file-input-wrapper">
        <label 
          htmlFor="pdf-file" 
          className={`file-input-label ${file ? 'has-file' : ''}`}
        >
          <svg 
            width="48" 
            height="48" 
            viewBox="0 0 24 24" 
            fill="none" 
            stroke="currentColor" 
            strokeWidth="2" 
            style={{ margin: '0 auto 1rem', color: file ? '#10b981' : '#6366f1' }}
          >
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
            <polyline points="17 8 12 3 7 8" />
            <line x1="12" y1="3" x2="12" y2="15" />
          </svg>
          <div className="file-input-text">
            {file ? (
              <>
                <div>Arquivo selecionado:</div>
                <div className="file-name">{file.name}</div>
              </>
            ) : (
              <>
                <div>Clique para selecionar um arquivo PDF</div>
                <div style={{ fontSize: '0.85rem', marginTop: '0.25rem' }}>ou arraste e solte aqui</div>
              </>
            )}
          </div>
        </label>
        <input
          id="pdf-file"
          type="file"
          accept="application/pdf"
          onChange={handleFileChange}
        />
      </div>
      
      <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <input
          type="text"
          placeholder="Nome completo"
          value={nome}
          onChange={(e) => setNome(e.target.value)}
          required
          style={{
            padding: '0.75rem 1rem',
            backgroundColor: 'var(--bg-tertiary)',
            border: '1px solid var(--border)',
            borderRadius: '8px',
            color: 'var(--text-primary)',
            fontSize: '1rem',
            fontFamily: 'inherit'
          }}
        />
        
        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
          style={{
            padding: '0.75rem 1rem',
            backgroundColor: 'var(--bg-tertiary)',
            border: '1px solid var(--border)',
            borderRadius: '8px',
            color: 'var(--text-primary)',
            fontSize: '1rem',
            fontFamily: 'inherit'
          }}
        />
        
        <input
          type="text"
          placeholder="CPF (apenas números)"
          value={cpf}
          onChange={(e) => setCpf(e.target.value.replace(/\D/g, ''))}
          maxLength={11}
          required
          style={{
            padding: '0.75rem 1rem',
            backgroundColor: 'var(--bg-tertiary)',
            border: '1px solid var(--border)',
            borderRadius: '8px',
            color: 'var(--text-primary)',
            fontSize: '1rem',
            fontFamily: 'inherit'
          }}
        />
      </div>
      
      <button type="submit" disabled={loading || !file || !nome || !email || !cpf}>
        {loading ? 'Processando...' : 'Assinar PDF'}
      </button>
      
      {error && <div className="error">{error}</div>}
    </form>
  );
}
export default SignPdfForm;
