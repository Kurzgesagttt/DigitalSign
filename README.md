# DigitalSign

Aplicação web para autenticar usuários, assinar arquivos PDF e verificar assinaturas pelo código/QR Code gerado pelo próprio sistema.

> A validação do DigitalSign é específica da aplicação. Não anuncie os resultados como assinatura qualificada ICP-Brasil nem como substituto de certificado ou orientação jurídica.

## Funcionalidades

- Cadastro, login e perfil de usuário.
- Envio e assinatura de PDF pelo backend; o documento inclui dados de verificação por QR Code.
- Verificação pública de uma assinatura por código, com dados pessoais limitados/mascarados.
- Painel administrativo para usuários, assinaturas, métricas, auditoria e relatórios CSV.
- API REST Spring Boot e interface React/TypeScript.

O sistema processa dados pessoais (incluindo CPF e dados de contato) e documentos enviados por usuários. Use apenas dados sintéticos em desenvolvimento e testes. Antes de uso público, defina e documente finalidade, base legal, retenção, resposta a incidentes e controles de acesso; o código não constitui garantia de conformidade legal.

## Arquitetura

- **Frontend:** React, TypeScript e Vite, servido pelo Nginx.
- **Backend:** Java 17 e Spring Boot; API sob `/v1`.
- **Banco:** PostgreSQL 16.
- **Containers:** Docker Compose. O Nginx encaminha `/api/` ao backend privado.

```text
Navegador --HTTPS--> Caddy/proxy --> frontend Nginx :8080
                                      | /api/*
                                      v
                                 backend :8080 --> PostgreSQL :5432
```

No Compose, somente o frontend publica porta no host (`127.0.0.1:8090`). API e banco não têm portas públicas; a rede e o volume `pgdata` são exclusivos da stack.

## Executar localmente com Docker

Requisitos: Docker com Compose v2 e Node.js 20+ (para gerar segredos ou rodar ferramentas do frontend).

PowerShell:

```powershell
Copy-Item .env.example .env
```

Linux/macOS:

```sh
cp .env.example .env
```

Edite `.env`: gere valores aleatórios diferentes para `POSTGRES_PASSWORD` e `JWT_SECRET`. Um exemplo de comando com Node.js para cada valor:

```sh
node -e "console.log(require('crypto').randomBytes(32).toString('base64url'))"
```

`JWT_SECRET` precisa ter pelo menos 32 bytes aleatórios. Nunca versione `.env`, nem reutilize os valores de exemplo ou credenciais de outros sistemas.

```sh
docker compose config --quiet
docker compose up -d --build
docker compose ps
```

Abra `http://localhost:8090` ou `http://127.0.0.1:8090`. O frontend chama a API pela rota `/api/`. O cadastro local aceita as duas origens. Veja [o guia de deploy local](docs/deploy-local.md) e importe o ambiente de `postman/` para testar a API. Para ver logs, use `docker compose logs --tail=100 backend frontend db`. `docker compose down` preserva o banco; `docker compose down -v` apaga permanentemente `pgdata`.

### Variáveis de ambiente

| Variável | Obrigatória | Uso |
|---|---:|---|
| `POSTGRES_PASSWORD` | Sim | Senha exclusiva do banco local/da stack. O Compose também a usa na conexão do backend. |
| `JWT_SECRET` | Sim | Segredo HMAC aleatório, exclusivo por ambiente, com pelo menos 32 bytes. |
| `APP_BASE_URL` | Não | Origem usada nos QR Codes; no local, `http://localhost:8090`; em produção, a URL HTTPS pública. |
| `CORS_ALLOWED_ORIGINS` | Não | Origens frontend permitidas, sem caminho; no local, `http://localhost:8090,http://127.0.0.1:8090`; em produção, a origem HTTPS exata. |
| `ADMIN_BOOTSTRAP_EMAIL` | Não | Promove a administrador uma conta já cadastrada com esse e-mail quando o backend inicia. Deixe vazio até escolher a conta. |

O Compose usa `digitalsign` como nome fixo do banco e do usuário PostgreSQL. Não há variáveis públicas separadas para a API ou para o banco. Para desenvolvimento do frontend fora do Compose, `DigitalSignatureFront/.env.example` configura `VITE_API_BASE_URL` para a API local. O build Docker de produção já usa `/api` no mesmo domínio.

## Deploy na VPS pelo Coolify

O repositório está preparado para um recurso **Docker Compose** apontando para a branch `main` e para `docker-compose.yml` na raiz.

1. No Coolify, conecte `https://github.com/Kurzgesagttt/DigitalSign`, branch `main`, Compose file `docker-compose.yml`.
2. Cadastre `POSTGRES_PASSWORD` e `JWT_SECRET` como variáveis secretas do recurso. Gere valores distintos, aleatórios e novos para produção; não use `.env.example` como fonte de segredo.
3. Defina `APP_BASE_URL=https://digitalsign.lucasmoreno.com.br` e `CORS_ALLOWED_ORIGINS=https://digitalsign.lucasmoreno.com.br`. Para habilitar o painel, defina `ADMIN_BOOTSTRAP_EMAIL` com o e-mail de uma conta já cadastrada; o backend precisa reiniciar para promover a conta.
4. Mantenha o Caddy existente do blog nas portas 80/443 e deixe o proxy Traefik do Coolify desligado. O recurso não precisa de domínio no proxy Coolify: o frontend já publica `127.0.0.1:8090` para o Caddy local.
5. Depois de conferir DNS, aponte apenas o subdomínio `digitalsign.lucasmoreno.com.br` para a VPS, preservando os outros registros. Acrescente ao Caddyfile existente:

   ```caddyfile
   digitalsign.lucasmoreno.com.br {
       reverse_proxy 127.0.0.1:8090
   }
   ```

6. Antes de recarregar Caddy, valide a configuração efetiva. Verifique o site e o blog por HTTPS após o deploy. Faça backup do banco antes de mudanças de schema e nunca use `docker compose down -v` para atualizar.

Não publique `8080` do backend nem `5432` do PostgreSQL. A porta 8090 deve estar livre e permanecer vinculada somente ao loopback da VPS.

## Desenvolvimento e verificações

Frontend:

```sh
cd DigitalSignatureFront
npm ci
npm test
npm run lint
npm run build
```

Backend:

```sh
cd DigitalSignature
mvn test
```

Compose e imagens:

```sh
docker compose config --quiet
docker compose build
```

O workflow `.github/workflows/ci.yml` executa testes, lint e build do frontend e testes do backend em pushes para `main` e pull requests.

## Endpoints principais

O Nginx recebe chamadas em `/api/v1/...` e encaminha para a API privada `/v1/...`.

| Método | Rota pública | Descrição |
|---|---|---|
| `POST` | `/api/v1/auth/register` | Cadastro |
| `POST` | `/api/v1/auth/login` | Login |
| `GET` | `/api/v1/sign` | Instruções de uso do endpoint de assinatura |
| `POST` | `/api/v1/sign` | Assinar PDF autenticado |
| `GET` | `/api/v1/verify/{code}` | Verificar assinatura |
| `GET` | `/api/v1/users/me` | Consultar perfil autenticado |
| `DELETE` | `/api/v1/users/me` | Excluir a própria conta |
| `GET` | `/api/v1/admin/users` | Listar usuários (admin) |
| `GET` | `/api/v1/admin/signatures` | Consultar assinaturas (admin) |
| `GET` | `/api/v1/admin/audit-logs` | Consultar auditoria (admin) |

Os demais endpoints administrativos estão em `DigitalSignature/src/main/java/com/Kurzgesagtt/DigitalSignature/controller/AdminController.java`.

## Estrutura

```text
DigitalSign/
├── docker-compose.yml
├── .env.example
├── .github/workflows/ci.yml
├── DigitalSignature/       # API Spring Boot e testes
├── DigitalSignatureFront/  # React/Vite, Nginx e testes
└── postman/                # Coleção de chamadas da API
```
