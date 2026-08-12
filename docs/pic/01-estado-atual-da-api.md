# Estado atual da API

## Visao geral observada

O DigitalSign e um prototipo com backend Spring Boot e frontend React/Vite. O fluxo atual permite cadastro de usuarios, login com JWT, assinatura de PDF por usuario autenticado, verificacao publica por codigo/QR Code e administracao por usuario com papel `ADMIN`.

Ao contrario do contexto antigo do PIC que descrevia o prototipo como "sem banco de dados", o codigo atual possui persistencia JPA em PostgreSQL no perfil principal e H2 em testes.

## Tecnologias e dependencias

| Area | Implementacao | Evidencia |
|---|---|---|
| Backend | Java 17, Spring Boot 3.5.0 | `DigitalSignature/pom.xml` |
| Web/API | `spring-boot-starter-web` | `DigitalSignature/pom.xml` |
| Seguranca | Spring Security, JWT `jjwt` 0.12.6, BCrypt | `SecurityConfig`, `JwtService`, `pom.xml` |
| Persistencia | Spring Data JPA, PostgreSQL runtime, H2 teste | `pom.xml`, `application.yml`, `application-test.yml` |
| PDF | Apache PDFBox 2.0.30 | `pom.xml`, `PdfMemorySignService` |
| Criptografia | BouncyCastle 1.70, RSA 2048, SHA-256 | `pom.xml`, `KeyGenerator`, `PdfMemorySignService`, `HashService` |
| QR Code | ZXing 3.5.3 | `QrCodeService`, `pom.xml` |
| Frontend | React 19.1.1, Vite 5.x, TypeScript, Axios | `DigitalSignatureFront/package.json`, `api.ts` |
| Containers | PostgreSQL 16-alpine, backend Java 17, frontend Nginx | `docker-compose.yml`, Dockerfiles |

## Modulos

| Modulo | Papel | Evidencia |
|---|---|---|
| `controller` | Endpoints de autenticacao, assinatura, verificacao, admin e erros | `AuthController`, `PdfSignController`, `VerificationController`, `AdminController`, `ApiExceptionHandler` |
| `services` | Assinatura PDF, QR Code, JWT, hash, auditoria, usuario autenticado e metadados de requisicao | `DigitalSignature/src/main/java/.../services/` |
| `model` | Entidades JPA de usuario, assinatura e auditoria | `Usuario`, `SignatureRecord`, `AuditLog` |
| `repository` | Repositorios JPA | `UsuarioRepository`, `SignatureRecordRepository`, `AuditLogRepository` |
| Frontend `components` | Telas de login, cadastro, assinatura, verificacao e admin | `DigitalSignatureFront/src/components/` |
| Frontend `api.ts` | Axios, armazenamento de token e usuario em `localStorage` | `DigitalSignatureFront/src/api.ts` |

## Endpoints

| Metodo | Rota | Entrada | Saida | Controle atual | Evidencia |
|---|---|---|---|---|---|
| `POST` | `/v1/auth/register` | JSON com nome, email, telefone, CPF, data nascimento, senha | JWT, role e dados do usuario | Publico; valida campos obrigatorios, CPF com 11 digitos, unicidade; senha BCrypt | `AuthController.register` |
| `POST` | `/v1/auth/login` | JSON email/senha | JWT, role e dados do usuario | Publico; bloqueio de conta; migra senha legada SHA-256 para BCrypt | `AuthController.login` |
| `GET` | `/v1/sign` | Nenhuma | Texto instrucional | `SecurityConfig` exige autenticacao apenas para `/v1/sign`; metodo GET tambem casa com rota | `PdfSignController.index`, `SecurityConfig` |
| `POST` | `/v1/sign` | Multipart `file` | PDF assinado, header `X-Verification-Code` | Autenticado; valida arquivo vazio e `Content-Type` igual a `application/pdf`; limite multipart 10 MB | `PdfSignController.signPdf`, `application.yml`, `AppConfig` |
| `GET` | `/v1/verify/{code}` | Codigo publico | Dados do assinante, CPF mascarado, status | Publico; registra auditoria | `VerificationController.verify` |
| `POST` | `/v1/verify/{code}` | CPF e email | Confirmacao ou erro | Publico; compara CPF/email com registro persistido | `VerificationController.confirmIdentity` |
| `GET` | `/v1/admin/users` | Paginacao | Usuarios com CPF mascarado | Exige role `ADMIN` | `AdminController.listUsers`, `SecurityConfig` |
| `PATCH` | `/v1/admin/users/{id}/block` | Motivo | Usuario atualizado | Exige `ADMIN`; impede bloquear propria conta | `AdminController.blockUser` |
| `PATCH` | `/v1/admin/users/{id}/unblock` | Nenhuma | Usuario atualizado | Exige `ADMIN` | `AdminController.unblockUser` |
| `GET` | `/v1/admin/signatures` | Filtros por codigo, usuario, status, datas | Assinaturas | Exige `ADMIN` | `AdminController.listSignatures` |
| `PATCH` | `/v1/admin/signatures/{id}/revoke` | Motivo | Assinatura revogada | Exige `ADMIN` | `AdminController.revokeSignature` |
| `GET` | `/v1/admin/metrics` | Nenhuma | Contagens e top assinantes | Exige `ADMIN` | `AdminController.metrics` |
| `GET` | `/v1/admin/audit-logs` | Paginacao | Logs de auditoria | Exige `ADMIN` | `AdminController.auditLogs` |
| `GET` | `/v1/admin/reports/signatures.csv` | Filtros | CSV de assinaturas | Exige `ADMIN` | `AdminController.exportSignaturesCsv` |
| `GET` | `/v1/admin/reports/audit-logs.csv` | Nenhuma | CSV de auditoria | Exige `ADMIN` | `AdminController.exportAuditLogsCsv` |

## Fluxo real do PDF

1. O usuario autenticado escolhe um arquivo no frontend. O campo HTML usa `accept="application/pdf"`, que e apenas filtro de interface.
2. O frontend envia `POST /v1/sign` com multipart usando Axios e JWT no header `Authorization`.
3. O backend rejeita arquivo ausente/vazio.
4. O backend confere somente `file.getContentType()` contra `application/pdf`.
5. O backend carrega todo o arquivo em memoria com `file.getBytes()`.
6. O backend gera codigo de verificacao com `UUID.randomUUID().toString().substring(0, 8)`.
7. O backend gera QR Code apontando para `app.base-url + "/verificar/" + code`.
8. O backend gera par RSA 2048 em memoria para aquela assinatura.
9. `PdfMemorySignService` carrega o PDF com PDFBox, adiciona metadados e uma pagina de certificado contendo nome, CPF, email, data, metodo e QR Code.
10. O servico adiciona uma assinatura PDF com `PDSignature` e `SHA256withRSA`.
11. O backend calcula SHA-256 do PDF original e assinado.
12. O backend persiste metadados da assinatura em `signature_records`, incluindo nome, email, CPF, IP, user-agent e hashes.
13. O PDF assinado e devolvido como download. O arquivo PDF completo nao foi observado como persistido no banco ou em diretorio de aplicacao.

```mermaid
flowchart LR
    U[Usuario autenticado] --> F[React SignPdfForm]
    F -->|multipart + Bearer JWT| API[POST /v1/sign]
    API --> V{Content-Type application/pdf?}
    V -- nao --> E[400 erro]
    V -- sim --> M[Carrega PDF inteiro em memoria]
    M --> Q[QR Code de verificacao]
    Q --> K[Par RSA 2048 em memoria]
    K --> P[PDFBox adiciona metadados/pagina]
    P --> S[Assinatura SHA256withRSA]
    S --> H[Hashes SHA-256 original/assinado]
    H --> DB[(PostgreSQL signature_records)]
    S --> D[Download PDF assinado]
    DB --> PUB[GET/POST /v1/verify/{code}]
    DB --> ADM[Rotas /v1/admin/**]
```

## Dados e locais de permanencia

| Local | Dados | Retencao observada |
|---|---|---|
| Tabela `usuarios` | Nome, email, telefone, CPF, data nascimento, senha hash, role, bloqueio | Indefinida; sem politica de exclusao/descarte no codigo |
| Tabela `signature_records` | Codigo, usuario, nome, email, CPF, data, status, IP, user-agent, hashes, revogacao | Indefinida; sem expiracao/retencao configurada |
| Tabela `audit_logs` | Email/role do ator, evento, resultado, alvo, IP, user-agent, detalhes | Indefinida; exporta ate 5000 logs por CSV |
| PDF assinado | Nome, email e CPF na pagina de certificado e metadados customizados | Permanece no arquivo baixado pelo usuario |
| Memoria da aplicacao | PDF original, PDF modificado, PDF assinado, QR Code, par RSA | Durante a requisicao; nao ha limpeza explicita de arrays |
| `localStorage` do navegador | JWT e usuario com nome, email, CPF, role | Indefinida ate logout/limpeza local |
| PostgreSQL volume Docker | Dados persistidos em `pgdata` | Ate remocao do volume |
| Logs de teste/runtime | Erros e stack traces internos em ambiente de teste; auditoria em banco | Variavel conforme ambiente |

## Chaves, certificados e algoritmos

| Item | Situacao | Evidencia |
|---|---|---|
| Chave de assinatura PDF | Par RSA 2048 gerado em memoria por requisicao; nao ha certificado X.509 persistido nem cadeia ICP-Brasil | `KeyGenerator.generateKeyPair`, `PdfMemorySignService.signPdf` |
| Assinatura PDF | `PDSignature` com filtro Adobe PPKLite e subfilter PKCS#7 detached; `Signature.getInstance("SHA256withRSA")` | `PdfMemorySignService` |
| Hash de integridade | SHA-256 em hexadecimal do PDF original e assinado | `HashService.sha256`, `PdfSignController.signPdf` |
| JWT | HMAC com segredo configurado, minimo de 32 bytes, expiracao padrao de 86400 segundos | `JwtService`, `application.yml` |
| Senhas de usuario | BCrypt para novas senhas; migracao de hash legado SHA-256 no login | `SecurityConfig.passwordEncoder`, `AuthController.senhaConfere` |
| HTTPS/TLS | Nao configurado no Spring Boot, Docker Compose ou Nginx analisados; endpoints locais usam HTTP | `application.yml`, `docker-compose.yml`, `nginx.conf` |
| Criptografia em repouso | Nao foi observada criptografia de colunas, arquivos, volume PostgreSQL ou PDF em repouso | ausencia de configuracao/codigo correspondente |

## Testes existentes

| Comando | Resultado | Observacao |
|---|---|---|
| `mvn test` em `DigitalSignature` | Sucesso: 5 testes, 0 falhas | Houve warning do PDFBox tentando gravar cache em `C:\Users\lucas\.pdfbox.cache` com acesso negado; build continuou |
| `npm test` em `DigitalSignatureFront` | Sucesso com permissao elevada: 2 arquivos, 3 testes | No sandbox inicial falhou com `spawn EPERM` do esbuild |
| `npm run lint` em `DigitalSignatureFront` | Sucesso | Sem saida de erros |
| `npm run build` em `DigitalSignatureFront` | Sucesso com permissao elevada | Bundle gerado em `dist/` |
| `npm audit --audit-level=high` | Sucesso: 0 vulnerabilidades reportadas | Cobre dependencias npm conforme base do npm audit |
| `mvn dependency:tree -Dscope=compile` | Sucesso com permissao elevada | Nao e varredura de vulnerabilidades; apenas inventario |
