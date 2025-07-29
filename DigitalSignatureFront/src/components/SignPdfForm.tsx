// components/SignPdfForm.tsx
import React, { useState } from 'react';
import { useSignPdf } from '../hooks/useSignPdf';

export const SignPdfForm = () => {
  const [file, setFile] = useState<File | null>(null);
  const { signPdf, loading, error } = useSignPdf();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (file) {
      await signPdf(file);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="file"
        accept="application/pdf"
        onChange={(e) => setFile(e.target.files?.[0] ?? null)}
      />
      <button type="submit" disabled={loading || !file}>
        {loading ? 'Enviando...' : 'Assinar PDF'}
      </button>
      {error && <p style={{ color: 'red' }}>{error}</p>}
    </form>
  );
};
