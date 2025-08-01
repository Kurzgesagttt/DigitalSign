import React, { useState } from 'react';
import type { ChangeEvent, FormEvent } from 'react';

import { useSignPdf } from '../hooks/useSignPdf';

export const SignPdfForm: React.FC = () => {
  const [file, setFile] = useState<File | null>(null);
  const { signPdf, loading, error } = useSignPdf();

  const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
    const selectedFile = e.target.files?.[0] || null;
    setFile(selectedFile);
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
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
        onChange={handleFileChange}
      />
      <button type="submit" disabled={loading || !file}>
        {loading ? 'Enviando...' : 'Assinar PDF'}
      </button>
      {error && <p style={{ color: 'red' }}>{error}</p>}
    </form>
  );
}
export default SignPdfForm;
