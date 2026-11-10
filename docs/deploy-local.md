# Deploy local com Docker

1. Copie `.env.example` para `.env` e preencha `POSTGRES_PASSWORD` e `JWT_SECRET` com valores distintos. O `JWT_SECRET` deve ter pelo menos 32 bytes.
2. Na raiz do projeto, execute `docker compose config --quiet` e `docker compose up -d --build`.
3. Abra `http://localhost:8090` ou `http://127.0.0.1:8090`. Cadastro e login usam o proxy em `/api/v1/auth/`.
4. Importe `postman/DigitalSign-PIC.postman_collection.json` e `postman/DigitalSign-local.postman_environment.json` no Postman. Selecione o ambiente local e preencha `demoPassword` antes das chamadas.

`apiBaseUrl` deve ser `http://localhost:8090/api`. Confira os serviços com `docker compose ps` e a API em `http://localhost:8090/api/actuator/health`. O banco fica apenas na rede do Compose. `docker compose down` para os containers sem apagar o banco; `docker compose down -v` apaga o volume do Postgres.

O Compose aceita as origens `localhost:8090` e `127.0.0.1:8090` por meio do `.env.example`. Se sobrescrever `CORS_ALLOWED_ORIGINS` no `.env`, inclua a origem exata da barra de endereços do navegador. Em produção, configure somente a origem HTTPS pública.
