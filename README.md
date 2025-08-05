# API de Assinatura Digital de Documentos

Este projeto é uma **API RESTful** para assinatura de documentos digitais em formato PDF. A proposta é permitir a assinatura com **validade jurídica**, utilizando certificados digitais do tipo **A1 (e-CPF)**.

> Atualmente, a API está em fase de testes e utiliza chaves RSA locais apenas para fins de desenvolvimento.  
> A assinatura gerada **não possui validade jurídica**.

---

## 🔍 Visão Geral

- **Assinatura de PDFs via API REST**
- **Frontend em React**
- **Backend em Java Spring Boot**
- Utiliza chaves RSA locais (sem certificado oficial, por enquanto)
- Assinatura pode ser verificada por leitores como **Adobe Acrobat Reader**

---

## 📅 Status do Projeto

- **Data da última atualização:** 04/10/2025  
- **Status atual:** Em desenvolvimento inicial  
- **Funcionalidades implementadas:**  
  - Upload de PDF
  - Assinatura com chave RSA
  - Retorno do PDF assinado
- **Limitações atuais:**  
  - Sem tratamento de erros
  - Assinatura inválida juridicamente (sem certificado A1)

---

##  Como Executar o Projeto

### Pré-requisitos

- Python 3 instalado
- Docker instalado

### Passo a passo

1. Navegue até a pasta `start-script`
2. Execute o script abaixo:

   ```bash
   python start.py
