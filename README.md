# 📝 Digital Signature - Assinador Digital de PDFs

API RESTful para assinatura digital de documentos PDF com interface web moderna e minimalista.

## 🎯 Sobre o Projeto

Sistema completo de assinatura digital de PDFs com:
- **Backend**: API REST em Java/Spring Boot
- **Frontend**: Interface React + TypeScript com design dark mode
- **Assinatura Digital**: Implementação usando chaves RSA (SHA256withRSA)
- **Certificado**: Geração automática de página de certificado no PDF assinado
- **Metadados**: Informações do assinante nos metadados do documento

> **⚠️ Importante**: Esta versão utiliza assinatura local com chaves RSA geradas automaticamente. Para assinaturas com validade jurídica, é necessário utilizar certificados digitais tipo A1 (e-CPF/e-CNPJ) emitidos por Autoridade Certificadora credenciada pelo ICP-Brasil.

## 🚀 Início Rápido

### Pré-requisitos

- [Docker](https://www.docker.com/get-started) instalado
- Python 3.x (para o script de inicialização)

### Instalação e Execução

1. Clone o repositório:
```bash
git clone https://github.com/Kurzgesagttt/DigitalSign.git
cd DigitalSign
```

2. Execute o script de inicialização:
```bash
python start_script/start.py
```

O script irá:
- Verificar se o Docker está instalado
- Construir as imagens dos containers
- Iniciar a aplicação automaticamente

3. Acesse a aplicação:
- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080

## 🏗️ Arquitetura

```
DigitalSign/
├── DigitalSignature/          # Backend (Java/Spring Boot)
│   ├── src/main/java/
│   │   └── com/Kurzgesagtt/DigitalSignature/
│   │       ├── controller/    # Endpoints REST
│   │       ├── services/      # Lógica de assinatura
│   │       └── config/        # Configurações (CORS, etc)
│   └── Dockerfile
├── DigitalSignatureFront/     # Frontend (React/TypeScript)
│   ├── src/
│   │   ├── components/        # Componentes React
│   │   └── hooks/             # Custom hooks
│   └── Dockerfile
├── start_script/              # Script Python de inicialização
└── docker-compose.yml         # Orquestração dos containers
```

## 📋 Funcionalidades

### Backend (API)
- ✅ Upload e processamento de arquivos PDF
- ✅ Geração de par de chaves RSA (2048 bits)
- ✅ Assinatura digital usando SHA256withRSA
- ✅ Adição de metadados personalizados no PDF
- ✅ Geração automática de página de certificado
- ✅ Suporte a CORS configurável
- ✅ API RESTful com Spring Boot

### Frontend
- ✅ Interface minimalista com tema escuro
- ✅ Upload de PDF via drag-and-drop
- ✅ Formulário com validação (Nome, Email, CPF)
- ✅ Download automático do PDF assinado
- ✅ Feedback visual durante processamento
- ✅ Design responsivo

### Certificado Digital Gerado
O PDF assinado inclui uma página adicional com:
- Nome completo do assinante
- CPF formatado
- Email para contato
- Data e hora da assinatura
- Método de assinatura utilizado
- Avisos legais

## 🔧 Tecnologias Utilizadas

### Backend
- Java 17
- Spring Boot 3.5.0
- Apache PDFBox 2.0.30
- Bouncy Castle (bcprov, bcpkix)
- Maven

### Frontend
- React 18
- TypeScript
- Vite
- Axios
- CSS Modules

### DevOps
- Docker & Docker Compose
- Python (script de automação)

## 📚 API Endpoints

### `POST /v1/sign`
Assina um documento PDF.

**Parameters:**
- `file` (multipart/form-data): Arquivo PDF
- `nome` (string): Nome completo do assinante
- `email` (string): Email do assinante
- `cpf` (string): CPF do assinante (apenas números)

**Response:**
- `200 OK`: Retorna o PDF assinado
- Content-Type: `application/pdf`
- Content-Disposition: `attachment; filename="documento_assinado.pdf"`

## 🛠️ Comandos Úteis

### Parar a aplicação:
```bash
docker compose down
```

### Reconstruir containers:
```bash
docker compose up --build
```

### Ver logs:
```bash
docker compose logs -f
```

## 🔒 Segurança

- As chaves RSA são geradas dinamicamente para cada assinatura
- Nenhuma chave privada é armazenada
- CORS configurado para ambientes de desenvolvimento
- Arquivos sensíveis incluídos no `.gitignore`

## 📝 Roadmap

- [ ] Suporte a certificados digitais tipo A1 (ICP-Brasil)
- [ ] Validação de assinatura digital
- [ ] Assinatura em lote (múltiplos PDFs)
- [ ] API de verificação de integridade
- [ ] Histórico de assinaturas
- [ ] Integração com serviços de certificação digital

## 👨‍💻 Desenvolvimento

### Backend (sem Docker)
```bash
cd DigitalSignature
./mvnw spring-boot:run
```

### Frontend (sem Docker)
```bash
cd DigitalSignatureFront
npm install
npm run dev
```

## 📄 Licença

Este projeto está sob desenvolvimento para fins educacionais e testes.

## 🤝 Contribuindo

Contribuições são bem-vindas! Sinta-se à vontade para abrir issues ou pull requests.

---

Desenvolvido por [Kurzgesagttt](https://github.com/Kurzgesagttt)

