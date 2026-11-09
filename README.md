# Digital Sign - PIC

## Deploy na VPS do blog com Coolify

Use o recurso Docker Compose apontando para esta branch e para `docker-compose.yml`. A stack tem rede e volume PostgreSQL próprios. Somente o frontend publica `127.0.0.1:8090:8080`; API e banco ficam privados. O Caddy existente pode encaminhar `digitalsign.lucasmoreno.com.br` para `127.0.0.1:8090`, sem iniciar o proxy Coolify nas portas 80/443.

No Coolify, cadastre `POSTGRES_PASSWORD` e `JWT_SECRET` como variáveis secretas. Configure `APP_BASE_URL` e `CORS_ALLOWED_ORIGINS` como `https://digitalsign.lucasmoreno.com.br`. Não configure domínio público para API ou banco.

Antes de publicar, confirme que a porta 8090 está livre, confira o DNS do subdomínio e faça backup do Caddyfile efetivo. Adicione apenas o bloco do subdomínio ao Caddyfile existente, valide com `caddy validate` e recarregue Caddy. Para rollback, remova esse bloco e recarregue Caddy; pare somente o recurso DigitalSign e preserve o volume PostgreSQL.

## Deploy do frontend na Vercel

O frontend React/Vite pode ser publicado na Vercel; o backend Spring Boot e o PostgreSQL precisam de hospedagem separada. Os arquivos `vercel.json` na raiz e em `DigitalSignatureFront/` cobrem as duas configurações possíveis de **Root Directory** do projeto Vercel. Ambos usam `npm ci`, `npm run build`, a pasta `dist` e fallback para rotas do React Router.

No projeto Vercel, configure `VITE_API_BASE_URL` com a URL **HTTPS** pública do backend, sem barra final. Essa variável é incorporada ao bundle no momento do build; depois de alterá-la, faça novo deploy. No backend, configure `CORS_ALLOWED_ORIGINS` com a origem exata da prévia ou do domínio usado, e `APP_BASE_URL` com a URL do frontend para que os QR Codes apontem ao ambiente correto. Sem uma API acessível, o frontend pode ser construído, mas cadastro, login, assinatura e verificação não funcionarão.

Para automação via Git, conecte o repositório ao projeto Vercel e selecione a branch de deploy desejada nas configurações do projeto. Confira a Root Directory antes de promover uma prévia. O projeto atual não inclui backend Java executável como função Vercel.

O workflow `.github/workflows/ci.yml` executa os testes e o build do frontend e os testes do backend em pushes para `main` e em pull requests. O roteiro de demonstração e a lista de evidências do PIC estão em `presentation/GUIA_PIC.md`.

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
cp .env.example .env
docker compose up --build
```

Antes de iniciar, preencha `POSTGRES_PASSWORD` e um `JWT_SECRET` aleatorio de pelo menos 32 bytes no arquivo `.env`, que nao deve ser versionado.

Serviços locais após `docker compose up --build`:

- Frontend: `http://localhost:8090`
- API via Nginx: `http://localhost:8090/api/`
- API e PostgreSQL não publicam portas no host.

As portas estão vinculadas a `127.0.0.1`; não ficam abertas para a rede externa.

Para parar:

```bash
docker compose down
```

Para reconstruir as imagens sem apagar o banco:

```bash
docker compose up --build
```

`docker compose down -v` apaga permanentemente os dados locais do PostgreSQL. Use apenas quando quiser reiniciar os dados de demonstração.

### Opção B: desenvolvimento local sem containerizar app

### 1. Subir o banco de dados (PostgreSQL)

```bash
docker compose up -d db
```

Para executar a API e o frontend fora dos containers, use um PostgreSQL local separado; o serviço `db` do Compose não publica a porta `5432`. A configuração do banco usa:
- **Database:** `digitalsign`
- **Usuário:** `digitalsign`
- **Senha:** valor de `POSTGRES_PASSWORD` no arquivo `.env`

No IntelliJ, abra `DigitalSignature` como projeto Maven e execute `DigitalSignatureApplication` com as variáveis `SPRING_DATASOURCE_PASSWORD` (o mesmo valor de `POSTGRES_PASSWORD`) e `JWT_SECRET` (ao menos 32 bytes aleatórios). Defina também `APP_BASE_URL=http://localhost:5173` para os QR Codes do frontend em desenvolvimento. O arquivo `.env` do Compose não é carregado automaticamente pela configuração Run do IntelliJ.

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
- Falha ao acessar `http://localhost:8090`: confirme que `frontend` e `backend` estão saudáveis com `docker compose ps`.
- Cache de build quebrado: use `docker compose build --no-cache` e depois `docker compose up`, preservando o volume do banco.

## Configuração

### Backend (`DigitalSignature/src/main/resources/application.yml`)

Todas as configurações podem ser sobrescritas via variáveis de ambiente:

| Variável | Padrão | Descrição |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/digitalsign` | URL do banco |
| `SPRING_DATASOURCE_USERNAME` | `digitalsign` | Usuário do banco |
| `SPRING_DATASOURCE_PASSWORD` | sem fallback | Senha do banco; obrigatoria |
| `SERVER_PORT` | `8080` | Porta do backend |
| `APP_BASE_URL` | `http://localhost:5173` | URL base do frontend (usada nos QR Codes) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:5174` | Origens CORS permitidas |
| `JWT_SECRET` | sem fallback | Segredo de assinatura JWT; obrigatorio e exclusivo por ambiente |
| `JWT_EXPIRATION_SECONDS` | `86400` | Tempo de validade do token |
| `ADMIN_BOOTSTRAP_EMAIL` | vazio | Promove para `ADMIN` um usuário já cadastrado com este email ao iniciar o backend |

### Frontend (`DigitalSignatureFront/.env`)

| Variável | Padrão | Descrição |
|---|---|---|
| `VITE_API_BASE_URL` | `http://localhost:8080` | URL da API backend |

## Fluxo de uso

1. Acesse `http://localhost:5173`
2. Crie uma conta (nome, email, telefone, CPF, data de nascimento, senha)
3. Faça login
4. Faça upload de um PDF e clique em "Assinar PDF"
5. O backend usa os dados do usuário autenticado para assinar o PDF
6. O PDF assinado terá uma página de certificado com um **QR Code de verificação**
7. Qualquer pessoa que escanear o QR Code pode confirmar a identidade do assinante informando CPF e email

## Admin

Para criar o primeiro admin em ambiente local:

1. Cadastre o usuário normalmente pelo frontend.
2. Reinicie o backend com `ADMIN_BOOTSTRAP_EMAIL=email-do-usuario`.
3. Faça login novamente; o link `Admin` aparecerá na navegação.

O painel admin permite listar usuários, bloquear/desbloquear contas, consultar assinaturas, revogar assinaturas, ver métricas, consultar auditoria e exportar CSV de assinaturas/auditoria.

## Endpoints principais

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/v1/auth/register` | Cadastro de usuário, retorna JWT e role |
| `POST` | `/v1/auth/login` | Login, retorna JWT e role |
| `POST` | `/v1/sign` | Assinar PDF autenticado (multipart: file) |
| `GET` | `/v1/verify/{code}` | Dados do assinante (mascarados) |
| `GET` | `/v1/admin/users` | Admin: listar usuários |
| `PATCH` | `/v1/admin/users/{id}/block` | Admin: bloquear usuário |
| `PATCH` | `/v1/admin/users/{id}/unblock` | Admin: desbloquear usuário |
| `GET` | `/v1/admin/signatures` | Admin: consultar assinaturas |
| `PATCH` | `/v1/admin/signatures/{id}/revoke` | Admin: revogar assinatura |
| `GET` | `/v1/admin/metrics` | Admin: métricas básicas |
| `GET` | `/v1/admin/audit-logs` | Admin: trilha de auditoria |
| `GET` | `/v1/admin/reports/signatures.csv` | Admin: exportar assinaturas em CSV |
| `GET` | `/v1/admin/reports/audit-logs.csv` | Admin: exportar auditoria em CSV |

Rotas admin exigem JWT de usuário com role `ADMIN`.

## Testes automatizados

Backend:

```bash
cd DigitalSignature
mvn test
```

Frontend:

```bash
cd DigitalSignatureFront
npm test
npm run lint
npm run build
```

Os testes de backend usam Spring Boot, MockMvc, Spring Security Test e H2 em memória. Os testes de frontend usam Vitest, jsdom e Testing Library.

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

