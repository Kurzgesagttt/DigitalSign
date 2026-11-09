import { useState } from 'react';
import axios from 'axios';
import { api } from '../api';

interface SignPdfParams {
  file: File;
}

export function useSignPdf() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function signPdf({ file }: SignPdfParams): Promise<void> {
    setLoading(true);
    setError(null);

    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await api.post('/v1/sign', formData, {
        responseType: 'blob',
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      const signedBlob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(signedBlob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'documento_assinado.pdf';
      a.click();
      window.URL.revokeObjectURL(url);
    } catch (err: unknown) {
      if (axios.isAxiosError(err) && err.response?.data instanceof Blob) {
        try {
          const text = await err.response.data.text();
          const json = JSON.parse(text) as { erro?: string };
          setError(json.erro || 'Erro ao assinar PDF.');
        } catch {
          setError('Erro ao assinar PDF.');
        }
      } else if (axios.isAxiosError(err)) {
        const data = err.response?.data as { erro?: string } | undefined;
        setError(data?.erro || 'Erro ao assinar PDF. Verifique se o backend esta rodando.');
      } else {
        setError('Erro ao assinar PDF. Verifique se o backend esta rodando.');
      }
    } finally {
      setLoading(false);
    }
  }

  return { signPdf, loading, error };
}
