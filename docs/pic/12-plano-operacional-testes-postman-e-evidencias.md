# Plano operacional de testes, Postman e evidencias do PIC

## Finalidade

Este roteiro transforma os objetivos do PIC em verificacoes reproduziveis para o prototipo DigitalSign. Ele complementa `06-matriz-rastreabilidade-pic.md`, `07-plano-de-implementacao.md`, `08-plano-de-testes-e-evidencias.md` e `11-diario-de-evidencias.md`.

O objetivo nao e certificar conformidade juridica com a LGPD ou afirmar que a assinatura possui cadeia ICP-Brasil. As evidencias devem demonstrar, com dados ficticios, quais controles estao implementados, quais riscos eles reduzem e quais lacunas permanecem.

## Regras para execucao e registro

- Usar somente usuarios, emails, CPFs e PDFs sinteticos. Nunca anexar documentos reais a Postman, commits, capturas ou ao diario.
- Registrar cada rodada no diario com data/hora, commit, ambiente, comando ou requisicao, resultado, evidencia armazenada e requisito relacionado.
- Para uma melhoria de seguranca, produzir duas evidencias equivalentes: a reproducao do comportamento anterior e a repeticao do mesmo caso apos a mudanca.
- Mascarar token JWT, CPF, email, IP, `Authorization` e conteudo de PDF em capturas. Preservar somente o necessario para comprovar status, headers, asserts e registros.
- Classificar a evidencia como `implementado`, `parcial`, `ausente/nao comprovado` ou `planejado`; nao descrever itens P0, P1 ou P2 como resultados antes de codigo, configuracao e teste correspondente.

## Organizacao da suite automatizada

### Niveis e responsabilidade

| Nivel | Onde organizar | Finalidade | Exemplos de verificacao |
|---|---|---|---|
| Unitario | `DigitalSignature/src/test/java/.../services` | Regras sem servidor, banco ou HTTP | SHA-256 deterministico, JWT valido/expirado, truncamento de auditoria e metadados de requisicao |
| Integracao de API | `DigitalSignature/src/test/java/.../controller` ou classes `*ApiIntegrationTest` | Contrato HTTP, Spring Security, JPA H2 e auditoria | cadastro, login, RBAC, assinatura multipart, verificacao e administracao |
| Regressao de seguranca | Mesmo modulo de integracao, agrupado por risco | Manter falhas de seguranca corrigidas fora do sistema | sem JWT, USER em rota ADMIN, upload falso, erros sem dados sensiveis |
| Frontend | `DigitalSignatureFront/src/**/*.test.tsx` e `*.test.ts` | Contratos do cliente e interfaces | header Bearer, logout, apresentacao mascarada e acesso a telas ADMIN |
| Manual de integracao | Colecao Postman executada contra a aplicacao em execucao | Demonstrar fluxo real entre cliente, API e banco | cadastro, assinatura, codigo de verificacao, consulta e bloqueio |
| Infraestrutura | Ambiente de teste/CI | Comprovar controles fora da aplicacao | TLS, configuracao por variaveis, scans de segredo e dependencias |

### Funcoes auxiliares recomendadas

Extrair os metodos privados da classe de integracao atual para uma classe de suporte de testes, por exemplo `TestFixtures`. Cada helper deve gerar dados previsiveis e descartaveis.

| Funcao sugerida | Comportamento | Uso |
|---|---|---|
| `createUser(email, cpf, role)` | Persiste um usuario com BCrypt, dados ficticios e papel solicitado | Preparar USER e ADMIN sem depender de endpoint administrativo |
| `login(email, senha)` | Faz `POST /v1/auth/login`, valida `200` e devolve somente o JWT | Reduz duplicacao em cenarios autenticados |
| `samplePdf()` | Gera em memoria um PDF minimo valido por PDFBox | Assinatura positiva sem arquivo binario no repositorio |
| `multipartPdf(nome, contentType, bytes)` | Cria `MockMultipartFile` com a chave `file` | Testes de upload valido, vazio e tipo incorreto |
| `registerBody(...)` | Monta o JSON valido de cadastro com valores padrao ficticios | Casos de cadastro e validacao de campos |
| `authorization(token)` | Produz `Bearer <token>` | Mantem os testes consistentes e evita omissao de header |
| `findLatestAudit(eventType)` | Consulta a auditoria apos uma acao | Demonstra rastreabilidade sem expor conteudo sensivel |

Os helpers pertencem somente ao codigo de teste. Eles nao devem introduzir rotas de suporte, credenciais especiais ou atalhos no codigo de producao.

### Classes de teste a manter ou criar

| Classe/grupo | Casos minimos | Evidencia do objetivo |
|---|---|---|
| `AuthApiIntegrationTest` | cadastro valido, campos ausentes, CPF invalido, email/CPF duplicado, login valido, senha invalida e conta bloqueada | Dados tratados no cadastro; BCrypt, autenticacao e resposta previsivel |
| `AuthorizationApiIntegrationTest` | `POST /v1/sign` sem JWT; USER em `/v1/admin/**`; ADMIN em rotas administrativas; token alterado/expirado quando o teste de `JwtService` estiver disponivel | Controle de acesso e autorizacao por papel |
| `PdfSignApiIntegrationTest` | PDF sintetico valido retorna PDF e `X-Verification-Code`; registro possui hashes de 64 caracteres e auditoria `SIGNATURE_CREATED` | Integridade tecnica, processamento em memoria e rastreabilidade |
| `PdfUploadSecurityTest` | arquivo vazio, MIME diferente, extensao `.pdf` com bytes comuns, PDF corrompido e arquivo acima do limite | Validacao segura de upload; os tres ultimos sao criterios P0 enquanto a validacao atual for baseada em `Content-Type` |
| `VerificationApiIntegrationTest` | codigo valido, inexistente, assinatura revogada e confirmacao de identidade com dados corretos/incorretos | Verificacao publica, exposicao minima e status de assinatura |
| `AdminApiIntegrationTest` | listar usuarios com CPF mascarado, bloquear/desbloquear, revogar assinatura, metricas, logs e CSV com ADMIN | Auditoria, RBAC e administracao |
| `ErrorAndAuditSecurityTest` | falha controlada sem stack trace no corpo; auditoria sem senha, JWT ou bytes do PDF | Tratamento seguro de erros, minimizacao e responsabilizacao |
| `api.test.ts` e testes de componentes | token aplicado/removido; usuario exibido sem CPF quando a melhoria P0 estiver feita; tela ADMIN apenas para role apropriada | Minimizacao no navegador e comportamento do frontend |

### Comandos de verificacao

Executar a partir dos diretorios indicados e anexar a saida relevante ao diario de evidencias:

```powershell
Set-Location DigitalSignature
mvn test

Set-Location ..\DigitalSignatureFront
npm test -- --run
npm run lint
npm run build
```

O profile `test` ja usa H2 em memoria e segredo JWT exclusivo de teste. O aviso atual do PDFBox sobre cache deve ser registrado como limitacao ambiental caso volte a ocorrer, sem incluir arquivos de cache como evidencia de negocio.

## Colecao Postman `DigitalSign PIC`

### Ambiente e pre-condicoes

Criar um ambiente Postman local, nao versionado com segredos, com as variaveis abaixo. Para execucao automatizada em CI, usar variaveis de ambiente do runner, nunca o arquivo pessoal de ambiente.

| Variavel | Exemplo/uso |
|---|---|
| `baseUrl` | `http://localhost:8080` ou URL HTTPS do ambiente de teste |
| `userEmail`, `userPassword`, `userCpf` | Identidade inteiramente ficticia do assinante |
| `adminEmail`, `adminPassword` | Identidade ficticia de administrador |
| `userToken`, `adminToken` | Preenchidos pelas respostas de login; nao salvar em evidencias |
| `verificationCode` | Preenchido pelo header da assinatura |
| `userId`, `signatureId` | Preenchidos pelas respostas administrativas quando aplicavel |
| `syntheticPdfPath` | Caminho local de um PDF sintetico escolhido no corpo multipart |

O endpoint de cadastro cria somente usuarios `USER`. Para executar os casos administrativos, cadastrar o `adminEmail`, reiniciar o ambiente de teste com `ADMIN_BOOTSTRAP_EMAIL` igual a esse email e entao efetuar o login. A promocao ocorre no startup por `AdminBootstrapConfig`; nao alterar role diretamente em producao para fins de demonstracao.

### Ordem da colecao

| Pasta/requisicao | Metodo e URL | Configuracao e asserts Postman | Variaveis/evidencia |
|---|---|---|---|
| `01 - Autenticacao / Cadastrar USER` | `POST {{baseUrl}}/v1/auth/register` | JSON valido ficticio; esperar `201`, `role = USER` e `token` presente | Salvar `userToken` e, se presente, o id; capturar status e campos mascarados |
| `01 - Autenticacao / Login USER` | `POST {{baseUrl}}/v1/auth/login` | Esperar `200`, `token` string e `role = USER` | Atualizar `userToken` |
| `02 - Assinatura / Sem JWT` | `POST {{baseUrl}}/v1/sign` | Multipart sem `Authorization`; esperar `401` ou `403` conforme configuracao Spring | Evidencia de bloqueio de rota |
| `02 - Assinatura / PDF valido` | `POST {{baseUrl}}/v1/sign` | Body `form-data`, campo `file` com PDF sintetico, `Authorization: Bearer {{userToken}}`; esperar `200`, `Content-Type: application/pdf` e header `X-Verification-Code` | Salvar `verificationCode`; baixar somente em diretorio temporario e nao publicar o PDF |
| `02 - Assinatura / Arquivo vazio` | `POST {{baseUrl}}/v1/sign` | Campo `file` vazio; esperar `400` | Evidencia de validacao atual |
| `02 - Assinatura / MIME invalido` | `POST {{baseUrl}}/v1/sign` | Arquivo sintetico enviado como `text/plain`; esperar `400` | Evidencia de validacao atual |
| `02 - Assinatura / Conteudo falso` | `POST {{baseUrl}}/v1/sign` | Arquivo `falso.pdf` com bytes de texto e MIME `application/pdf`; esperar `400` apos a melhoria P0 | Marcar como pendente enquanto a API aceitar o arquivo |
| `02 - Assinatura / PDF corrompido` | `POST {{baseUrl}}/v1/sign` | PDF truncado; esperar `400` padronizado e sem stack trace apos P0 | Evidencia antes/depois |
| `03 - Verificacao / Consultar codigo` | `GET {{baseUrl}}/v1/verify/{{verificationCode}}` | Esperar `200`, `valido = true`, CPF mascarado e ausencia de token | Evidencia de consulta publica e minimizacao parcial |
| `03 - Verificacao / Confirmar identidade correta` | `POST {{baseUrl}}/v1/verify/{{verificationCode}}` | JSON com CPF/email ficticios corretos; esperar `200` e `confirmado = true` | Nao capturar CPF completo |
| `03 - Verificacao / Confirmar identidade incorreta` | Mesmo endpoint | Alterar um campo; esperar `200` e `confirmado = false` | Evidencia de rejeicao controlada |
| `04 - Administracao / Login ADMIN` | `POST {{baseUrl}}/v1/auth/login` | Pre-condicao de bootstrap atendida; esperar `200` e `role = ADMIN` | Salvar `adminToken` |
| `04 - Administracao / USER em rota ADMIN` | `GET {{baseUrl}}/v1/admin/users` | `Authorization: Bearer {{userToken}}`; esperar `403` | Evidencia RBAC negativo |
| `04 - Administracao / Listar usuarios` | `GET {{baseUrl}}/v1/admin/users` | `Authorization: Bearer {{adminToken}}`; esperar `200`, `content` array e CPF mascarado | Salvar `userId` se necessario |
| `04 - Administracao / Bloquear e testar login` | `PATCH {{baseUrl}}/v1/admin/users/{{userId}}/block`, seguido de login USER | JSON com motivo ficticio; esperar `200` e `bloqueado = true`; login posterior `403` | Evidencia de autorizacao e auditoria |
| `04 - Administracao / Listar assinaturas` | `GET {{baseUrl}}/v1/admin/signatures?code={{verificationCode}}` | `Authorization: Bearer {{adminToken}}`; esperar `200` e localizar a assinatura pelo codigo | Salvar `signatureId` do primeiro item retornado |
| `04 - Administracao / Metricas e auditoria` | `GET {{baseUrl}}/v1/admin/metrics` e `GET {{baseUrl}}/v1/admin/audit-logs` | Esperar `200`; confirmar eventos sem senha, token ou conteudo PDF | Evidencia de responsabilizacao; revisar dados expostos |
| `04 - Administracao / Revogar assinatura` | `PATCH {{baseUrl}}/v1/admin/signatures/{{signatureId}}/revoke`, seguido de `GET {{baseUrl}}/v1/verify/{{verificationCode}}` | Esperar revogacao e depois `valido = false` | Evidencia de ciclo de status |

Para requests com corpo JSON, definir `Content-Type: application/json`. Para assinatura, deixar o Postman gerar o limite `multipart/form-data` e nao fixar manualmente o header.

### Scripts Postman reutilizaveis

Adicionar a cada request de login o script abaixo, ajustando `userToken` para `adminToken` no login administrativo:

```javascript
pm.test("login retornou token", function () {
  pm.response.to.have.status(200);
  const body = pm.response.json();
  pm.expect(body.token).to.be.a("string").and.not.empty;
  pm.environment.set("userToken", body.token);
});
```

No request de assinatura valida, capturar o codigo sem incluir o PDF em exportacoes:

```javascript
pm.test("PDF assinado retornado", function () {
  pm.response.to.have.status(200);
  pm.expect(pm.response.headers.get("Content-Type")).to.include("application/pdf");
  const code = pm.response.headers.get("X-Verification-Code");
  pm.expect(code).to.match(/^[a-z0-9-]{8}$/i);
  pm.environment.set("verificationCode", code);
});
```

No acesso administrativo, verificar a minimizacao atualmente aplicavel:

```javascript
pm.test("CPF administrativo esta mascarado", function () {
  pm.response.to.have.status(200);
  const first = pm.response.json().content[0];
  pm.expect(first.cpf).to.match(/^\d{3}\.\*\*\*\.\*\*\*-\d{2}$/);
});
```

No request `Listar usuarios`, escolher no script de teste o registro cujo email e `{{userEmail}}` e salvar seu `id` em `userId`. No request `Listar assinaturas`, salvar o `id` do primeiro item de `content` em `signatureId`. Falhar explicitamente se os itens esperados nao forem retornados; isso evita que uma acao administrativa seja direcionada a outro registro.

## Matriz de demonstracao dos objetivos do PIC

| Objetivo especifico | Funcoes ou mecanismo observavel | Teste/evidencia | Estado e limite a declarar |
|---|---|---|---|
| Identificar dados e ativos tratados | Entidades `Usuario`, `SignatureRecord`, `AuditLog`; frontend `api.ts`; fluxo de assinatura | Tabelas de `01-estado-atual-da-api.md` e teste que confirma persistencia de assinatura/auditoria | Inventario documental; retencao ainda indefinida |
| Mapear locais de processamento e fronteiras de confianca | Navegador, HTTP, memoria, PostgreSQL, logs, verificacao publica e admin | Diagrama de fluxo, Docker/configuracao e rodada Postman completa | TLS e criptografia em repouso nao comprovados |
| Diferenciar mecanismos criptograficos | BCrypt, JWT HMAC, SHA-256, `SHA256withRSA`, configuracao TLS ausente | Testes unitarios de hash/JWT, teste de BCrypt e inspecao do PDF assinado | Hash e assinatura nao entregam confidencialidade; sem certificado X.509 confiavel/ICP-Brasil |
| Avaliar autenticacao, autorizacao, upload, erros, logs e segredos | `SecurityConfig`, `JwtService`, controllers, `AuditService`, `ApiExceptionHandler` | Sem JWT, USER em ADMIN, login bloqueado, PDF vazio/MIME invalido e auditoria | `anyRequest().permitAll()`, segredos de desenvolvimento e validacao de conteudo PDF sao lacunas atuais |
| Identificar e classificar riscos CIA | Matriz de ameacas e cenarios negativos | Resultado esperado/falha observada para cada caso em `08` e nesta colecao | Classificacao qualitativa, nao uma medicao quantitativa de risco |
| Relacionar controles a LGPD | Controle de acesso, minimizacao, auditoria, testes e descarte planejado | Matriz de rastreabilidade e diario com evidencia ligada ao requisito | Apoio aos principios e aos arts. 46 e 49; nao e parecer juridico |
| Consolidar testes e produzir antes/depois | Maven, Vitest, Postman e testes negativos repetiveis | Saidas, asserts, codigo de teste e registro no diario | Comparar o mesmo dado sintetico no mesmo ambiente |
| Priorizar melhorias sem antecipa-las | Backlog P0/P1/P2 | Cada item tem criterio de aceite e teste correspondente | P0/P1/P2 sao planejamento ate a evidencia existir |
| Esclarecer limites da assinatura atual | RSA 2048 efemero, assinatura PDF, hashes e QR Code | PDF assinado, header de verificacao e consulta publica | Nao prova identidade externa, validade juridica ou cadeia ICP-Brasil |

## Casos condicionados a melhorias futuras

| Prioridade | Teste de aceite a adicionar | Resultado esperado |
|---|---|---|
| P0 - validacao de PDF | Bytes nao-PDF com MIME `application/pdf`; PDF corrompido; limite excedido | `400` padronizado para formato/corrupcao, `413` ou erro definido para limite, sem stack trace |
| P0 - negacao por padrao | Requisicao a uma rota futura nao liberada explicitamente | `401` ou `403`, nunca acesso publico implicito |
| P0 - TLS | Cliente TLS contra ambiente de teste | Certificado e protocolo negociados; HTTP redirecionado ou recusado conforme arquitetura |
| P0 - minimizacao | Inspecao de resposta, `localStorage` e PDF resultante | CPF e outros dados apenas quando indispensaveis e justificados |
| P1 - rate limiting | Repetir login, verificacao e assinatura acima da cota | `429` com politica documentada, sem bloquear casos legitimos fora da cota |
| P1 - logs e erros | Forcar falha com dados ficticios sensiveis | Resposta e auditoria nao contem senha, token, CPF completo ou conteudo do PDF |
| P1 - retencao/descarte | Criar registro com data expirada e executar job | Registro removido ou anonimizado conforme politica documentada |
| P1 - CI | Pipeline em pull request | Testes, lint, build, scan de dependencias e scan de segredos executados e arquivados |
| P2 - criptografia em repouso | Inspecionar dump/volume e configuracao de chaves autorizada | Dados ilegiveis fora da aplicacao; chaves fora do repositorio e com recuperacao/rotacao documentada |
| P2 - PKI | Validacao do PDF com certificado/cadeia aplicavel | Cadeia e confianca documentadas; nao assumir ICP-Brasil sem certificado correspondente |

## Criterios de conclusao de uma rodada

Uma rodada esta pronta para uso no relatorio quando: os testes automatizados aplicaveis passaram; a colecao Postman foi executada na ordem definida; os resultados negativos tiveram status e corpo avaliados; a evidencia foi mascarada; e o diario registra a relacao entre objetivo, risco, controle, lacuna e resultado.

Quando um caso revelar uma lacuna, o resultado esperado do PIC e a sua documentacao honesta, seguida de item priorizado no backlog. A lacuna nao deve ser ocultada nem convertida em afirmacao de conformidade.
