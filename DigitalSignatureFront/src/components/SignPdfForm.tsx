import React, { useState } from 'react';
import type { ChangeEvent, FormEvent } from 'react';

import { useSignPdf } from '../hooks/useSignPdf';

interface User {
  id: number;
  nome: string;
  email: string;
  cpf: string;
}

interface Props {
  user: User;
}

export const SignPdfForm: React.FC<Props> = ({ user }) => {
  const [file, setFile] = useState<File | null>(null);
  const { signPdf, loading, error } = useSignPdf();

  const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
    const selectedFile = e.target.files?.[0] || null;
    setFile(selectedFile);
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (file) {
      await signPdf({ file, nome: user.nome, email: user.email, cpf: user.cpf });
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <div style={{ marginBottom: '1rem', padding: '0.75rem 1rem', backgroundColor: 'var(--bg-tertiary)', borderRadius: '8px', border: '1px solid var(--border)' }}>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', margin: 0 }}>
          Assinando como: <strong style={{ color: 'var(--text-primary)' }}>{user.nome}</strong> ({user.email})
        </p>
      </div>

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
      
      <button type="submit" disabled={loading || !file}>
        {loading ? 'Processando...' : 'Assinar PDF'}
      </button>
      
      {error && <div className="error">{error}</div>}
    </form>
  );
}
export default SignPdfForm;
