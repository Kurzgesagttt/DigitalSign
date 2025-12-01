import { useState } from 'react';
import axios from 'axios';

interface SignPdfParams {
  file: File;
  nome: string;
  email: string;
  cpf: string;
}

export function useSignPdf() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const apiBaseUrl = 'http://localhost:8080';  //colocar no .env npm r

  async function signPdf({ file, nome, email, cpf }: SignPdfParams): Promise<void> {
    setLoading(true);
    setError(null);

    const formData = new FormData();
    formData.append('file', file);
    formData.append('nome', nome);
    formData.append('email', email);
    formData.append('cpf', cpf);

    try {
      const response = await axios.post(`${apiBaseUrl}/v1/sign`, formData, {
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
    } catch (err) {
      setError('Erro ao assinar PDF. Verifique se o backend está rodando.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  }

  return { signPdf, loading, error };
}
