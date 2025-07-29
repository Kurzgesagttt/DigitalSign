import { useState } from 'react';
import axios from 'axios';

export function useSignPdf() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function signPdf(file: File): Promise<void> {
    setLoading(true);
    setError(null);

    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await axios.post(
        'http://localhost:8080/v1/sign', // URL da sua API
        formData,
        {
          responseType: 'blob', // importante: estamos esperando um PDF binário
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        }
      );

      const signedBlob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(signedBlob);
      // Força o download
      const a = document.createElement('a');
      a.href = url;
      a.download = 'signed.pdf';
      a.click();
      window.URL.revokeObjectURL(url);
    } catch (err: any) {
      setError('Erro ao assinar PDF.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  }

  return { signPdf, loading, error };
}
