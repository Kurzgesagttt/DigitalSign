import { useState } from 'react';
import axios from 'axios';

const API = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

interface SignPdfParams {
  file: File;
  nome: string;
  email: string;
  cpf: string;
}

export function useSignPdf() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function signPdf({ file, nome, email, cpf }: SignPdfParams): Promise<void> {
    setLoading(true);
    setError(null);

    const formData = new FormData();
    formData.append('file', file);
    formData.append('nome', nome);
    formData.append('email', email);
    formData.append('cpf', cpf);

    try {
      const response = await axios.post(`${API}/v1/sign`, formData, {
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
    } catch (err: any) {
      if (err?.response?.data instanceof Blob) {
        try {
          const text = await err.response.data.text();
          const json = JSON.parse(text);
          setError(json.erro || 'Erro ao assinar PDF.');
        } catch {
          setError('Erro ao assinar PDF.');
        }
      } else {
        setError('Erro ao assinar PDF. Verifique se o backend está rodando.');
      }
      console.error(err);
    } finally {
      setLoading(false);
    }
  }

  return { signPdf, loading, error };
}
