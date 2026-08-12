# Analise de programacao segura

## Checklist de controles

| Controle | Situacao | Evidencia | Impacto/Lacuna | Recomendacao |
|---|---|---|---|---|
| Minimizacao dos dados recebidos | Parcial | `AuthController.RegisterRequest`, `RegisterPage` | Coleta telefone e data de nascimento sem uso tecnico observado; CPF completo armazenado e embutido no PDF | Remover campos nao essenciais ou justificar no PIC |
| Validacao real do PDF | Parcial | `PdfSignController.signPdf` | Confere apenas `Content-Type`; nao valida magic bytes, estrutura PDF, criptografia, scripts ou anexos | Validar assinatura `%PDF-`, parse controlado, tipo real e casos negativos |
| Limite de tamanho | Implementado parcial | `application.yml`, `AppConfig` | 10 MB configurado, mas duplicado e sem teste negativo de limite | Centralizar config e testar arquivo acima do limite |
| Controle de tempo/memoria | Parcial | `file.getBytes()`, `ByteArrayOutputStream` | PDF inteiro e versoes intermediarias em memoria; risco de DoS CPU/memoria | Processamento com limites, timeout e fila se necessario |
| Arquivos maliciosos/PDF ativo | Nao implementado | Nao foi encontrada sanitizacao | PDFs podem conter scripts, anexos, formularios e payloads complexos | Sanitizacao/normalizacao ou rejeicao de recursos ativos |
| Autenticacao | Implementado | `SecurityConfig`, `JwtAuthenticationFilter`, `JwtService` | `/v1/sign` e admin protegidos; `/v1/verify` publico por desenho | Manter e testar rotas novas como privadas por padrao |
| Autorizacao por objeto | Parcial | `AdminController`, `AuthenticatedUserService` | Admin pode consultar tudo; nao ha endpoint de documentos por usuario | Definir escopo; se houver persistencia por usuario, testar BOLA/IDOR |
| RBAC | Implementado parcial | `UserRole`, `SecurityConfig` | `ADMIN` para `/v1/admin/**`; bootstrap por email | Auditar promocao admin e registrar controles operacionais |
| CORS | Parcial | `AppConfig`, `application.yml` | Origens parametrizadas; metodos/headers liberados com credenciais | Restringir metodos/headers e validar origem em producao |
| HTTPS/TLS | Nao implementado no repo | `application.yml`, `docker-compose.yml`, `nginx.conf` usam HTTP | Dados em transito dependem de infraestrutura externa nao documentada | Configurar TLS no proxy/ambiente ou documentar terminacao |
| Criptografia em repouso | Nao implementado | Entidades JPA sem criptografia; Compose sem volume criptografado | Banco contem dados pessoais diretos | Avaliar criptografia de disco/colunas e gestao de chaves |
| Aleatoriedade de IDs | Parcial | `UUID.randomUUID().substring(0, 8)` | Codigo publico com entropia reduzida | Usar token URL-safe de 128 bits ou mais |
| Algoritmos criptograficos | Parcial | RSA 2048, SHA-256, HMAC JWT | Assinatura PDF nao usa certificado confiavel; chave efemera nao e verificavel por cadeia | Definir objetivo: demonstracao tecnica vs PKI real |
| Segredos | Parcial/critico | `.env`, `application.yml`, `docker-compose.yml` contem valores padrao/dev | Segredos e senhas padrao versionados; nao reproduzidos aqui | Rotacionar, mover para secret manager/env local e limpar historico se necessario |
| Path traversal/escrita insegura | Implementado no fluxo atual | Nao ha uso do nome do arquivo para gravar em disco | Baixo no fluxo atual | Manter sem escrita por nome do usuario |
| Temporarios | Parcial | Processamento em memoria; PDFBox tentou cache de fonte no home durante teste | Nao ha temp de PDF observado; ha escrita tentada de cache PDFBox | Configurar `pdfbox.fontcache` em diretorio controlado/temporario |
| Limpeza de buffers | Nao implementado | Arrays `originalPdf`, `signedPdf`, QR e chave em memoria | Dados permanecem ate GC | Reduzir copias; limpar arrays quando aplicavel |
| Logs sem dados pessoais | Parcial | `AuditService.record`, `console.error` frontend | Auditoria grava email, IP, user-agent e detalhes; login inexistente grava email digitado; frontend loga erro | Mascarar dados, padronizar detalhes e remover `console.error` em producao |
| Erros seguros | Parcial | `PdfSignController` retorna erro generico; `ApiExceptionHandler` devolve `ex.getMessage()` | Alguns erros podem expor detalhes internos em endpoints admin/gerais | Respostas padronizadas com codigo de correlacao |
| Dependencias vulneraveis | Nao verificavel completamente | `npm audit` 0 vuln; Maven dependency tree gerada | Nao foi executado OWASP Dependency-Check/Snyk para Maven | Adicionar scan CI |
| Rate limiting/abuso | Nao implementado | Ausencia de limiter | Login, verificacao e upload podem sofrer abuso | Rate limiting por IP/usuario e bloqueio progressivo |
| Retencao/descarte | Nao implementado | Entidades sem expiracao; volume `pgdata` | Dados pessoais e logs permanecem indefinidamente | Politica de retencao e jobs de expurgo |
| Segregacao de ambientes | Parcial | `application-test.yml`, Docker Compose local | Segredos padrao em dev; sem perfis prod seguros | Perfis `dev/test/prod` com validacao de segredos |
| Testes negativos de seguranca | Parcial | `DigitalSignatureApiIntegrationTest` | Sem MIME falso, arquivo vazio/corrompido/acima do limite, logs e TLS | Adicionar suite de seguranca |

## Achados por severidade

| Severidade | Achado | Evidencia | Impacto | Recomendacao |
|---|---|---|---|---|
| Critica | Segredos/senhas padrao de banco e JWT aparecem em arquivos versionados | `.env`, `application.yml`, `docker-compose.yml` | Uso acidental em ambiente real, acesso indevido, falsificacao de JWT se segredo for reutilizado | Rotacionar valores, remover de versao, usar `.env.example` sem segredo real |
| Alta | Ausencia de HTTPS/TLS na configuracao analisada | `nginx.conf`, `docker-compose.yml`, `application.yml` | Interceptacao de PDF, CPF, email, senha e JWT em transito se usado fora de ambiente local protegido | Configurar TLS no proxy/ambiente e registrar evidencia |
| Alta | Validacao de PDF baseada apenas em `Content-Type` | `PdfSignController.signPdf` | Arquivo falso/malicioso pode chegar ao PDFBox; risco de abuso e falhas de processamento | Validacao real e testes negativos |
| Alta | Retencao indefinida de dados pessoais e logs | Entidades `Usuario`, `SignatureRecord`, `AuditLog` | Exposicao prolongada e dificuldade de demonstrar necessidade | Politica e job de retencao/descarte |
| Alta | Endpoint de verificacao publico expõe nome/email e permite confirmacao por CPF/email sem rate limiting | `VerificationController` | Enumeracao/tentativas de validacao de identidade por codigo | Codigo mais forte, rate limiting, respostas menos informativas |
| Media | `localStorage` armazena JWT e CPF | `DigitalSignatureFront/src/api.ts` | Token acessivel a XSS; CPF no navegador | Reduzir dados do usuario armazenados e avaliar cookie HttpOnly quando aplicavel |
| Media | `anyRequest().permitAll()` libera endpoints nao mapeados explicitamente | `SecurityConfig` | Rotas futuras podem ficar publicas por erro | Trocar para `anyRequest().authenticated()` ou negar por padrao |
| Media | PDF assinado contem CPF completo em pagina/metadados | `PdfMemorySignService` | Dados pessoais se propagam no documento sem minimizacao | Mascarar CPF ou tornar inclusao opcional/justificada |
| Media | Logs/auditoria podem conter dados pessoais em detalhes | `AuditService`, controllers | Retencao de email/codigo/motivo livre | Catalogar eventos e mascarar detalhes |
| Baixa | Warning de cache PDFBox no home do usuario durante testes | Resultado `mvn test` | Escrita fora de diretorio controlado; ruido operacional | Configurar diretorio de cache controlado |

## Relacao OWASP

- OWASP API Security Top 10: riscos relacionados a BOLA/IDOR quando houver objetos persistidos por usuario, autenticacao, autorizacao, consumo irrestrito de recursos e configuracao incorreta.
- OWASP File Upload Cheat Sheet: validar conteudo real, limitar tamanho, nao confiar em extensao/MIME do cliente, processar em ambiente controlado.
- OWASP ASVS: controles de autenticacao, sessao/token, validacao de entrada, arquivos, criptografia, erros/logs e configuracao segura.
