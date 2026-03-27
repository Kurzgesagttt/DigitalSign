# DigitalSign - Assinador Digital de PDF

Sistema web para assinatura digital de documentos PDF com verificação por QR Code.

## Pré-requisitos

- **Java 17** (JDK)
- **Maven** (ou usar o wrapper do IDE)
- **Node.js 20+** e **npm**
- **Docker** e **Docker Compose v2** (para rodar stack completa em containers)

## Como rodar localmente

### Opção A: stack completa com Docker (recomendado para outro PC)

Na raiz do projeto:

```bash
docker compose up --build
```

Serviços disponíveis:
- Frontend: `http://localhost:8080`
- Backend (direto): `http://localhost:8081`
- Banco PostgreSQL: `localhost:5432`

Para parar:

```bash
docker compose down
```

Para rebuild limpo (quando trocar de máquina ou após mudanças grandes):

```bash
docker compose down -v
docker compose build --no-cache
docker compose up
```

### Opção B: desenvolvimento local sem containerizar app

### 1. Subir o banco de dados (PostgreSQL)

```bash
docker compose up -d db
```

Isso inicia um PostgreSQL na porta `5432` com:
- **Database:** `digitalsign`
- **Usuário:** `digitalsign`
- **Senha:** `digitalsign123`

### 2. Iniciar o Backend (porta 8080)

```bash
cd DigitalSignature
mvn spring-boot:run -DskipTests
```

O backend ficará disponível em `http://localhost:8080`.

API docs (Swagger): `http://localhost:8080/swagger-ui.html`

### 3. Iniciar o Frontend (porta 5173)

```bash
cd DigitalSignatureFront
npm install
npm run dev
```

O frontend ficará disponível em `http://localhost:5173`.

## Troubleshooting de Docker

- Erro de build dizendo que não encontrou `DigitalSignature/Dockerfile` ou `DigitalSignatureFront/Dockerfile`:
    atualize para a versão mais recente do repositório (esses arquivos devem existir).
- Porta em uso (`5432`, `8080` ou `8081`):
    pare processos locais que estejam usando essas portas ou altere no `docker-compose.yml`.
- Cache quebrado após trocar de PC:
    use `docker compose down -v` e depois `docker compose build --no-cache`.

## Configuração

### Backend (`DigitalSignature/src/main/resources/application.yml`)

Todas as configurações podem ser sobrescritas via variáveis de ambiente:

| Variável | Padrão | Descrição |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/digitalsign` | URL do banco |
| `SPRING_DATASOURCE_USERNAME` | `digitalsign` | Usuário do banco |
| `SPRING_DATASOURCE_PASSWORD` | `digitalsign123` | Senha do banco |
| `SERVER_PORT` | `8080` | Porta do backend |
| `APP_BASE_URL` | `http://localhost:5173` | URL base do frontend (usada nos QR Codes) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:5174` | Origens CORS permitidas |

### Frontend (`DigitalSignatureFront/.env`)

| Variável | Padrão | Descrição |
|---|---|---|
| `VITE_API_BASE_URL` | `http://localhost:8080` | URL da API backend |

## Fluxo de uso

1. Acesse `http://localhost:5173`
2. Crie uma conta (nome, email, telefone, CPF, data de nascimento, senha)
3. Faça login
4. Faça upload de um PDF e clique em "Assinar PDF"
5. O PDF assinado terá uma página de certificado com um **QR Code de verificação**
6. Qualquer pessoa que escanear o QR Code pode confirmar a identidade do assinante informando CPF e email

## Endpoints principais

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/v1/auth/register` | Cadastro de usuário |
| `POST` | `/v1/auth/login` | Login |
| `POST` | `/v1/sign` | Assinar PDF (multipart: file, nome, email, cpf) |
| `GET` | `/v1/verify/{code}` | Dados do assinante (mascarados) |
| `POST` | `/v1/verify/{code}` | Confirmar identidade (body: cpf, email) |

## Estrutura

```
DigitalSign/
├── docker-compose.yml          # PostgreSQL local
├── DigitalSignature/            # Backend (Spring Boot + Java 17)
│   └── src/main/java/com/Kurzgesagtt/DigitalSignature/
│       ├── controller/          # AuthController, PdfSignController, VerificationController
│       ├── model/               # Usuario, SignatureRecord
│       ├── repository/          # JPA repositories
│       └── services/            # PdfMemorySignService, QrCodeService, KeyGenerator
└── DigitalSignatureFront/       # Frontend (React + TypeScript + Vite)
    └── src/
        ├── components/          # LoginPage, RegisterPage, SignPdfForm, VerifyPage
        └── hooks/               # useSignPdf
```

