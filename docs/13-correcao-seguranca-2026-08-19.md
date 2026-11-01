# Correcoes de seguranca - 19 de agosto de 2026

## Escopo e limite

Este documento registra as correcoes aplicadas apos a revisao da branch `pic_doc_validacao`. Ele descreve controles tecnicos observaveis; nao constitui certificacao de conformidade com a LGPD, nem declara assinatura ICP-Brasil ou identidade juridica verificavel.

As verificacoes e os testes continuam restritos a dados e PDFs sinteticos.

## Alteracoes implementadas

| Tema | Alteracao | Evidencia no codigo |
|---|---|---|
| Assinatura PDF | A resposta de assinatura agora contem CMS/PKCS#7 detached gerado com Bouncy Castle; removeu-se o pre-hash seguido de `SHA256withRSA`, que causava hash duplo. Um certificado X.509 efemero e autoassinado acompanha a assinatura apenas para permitir verificacao estrutural. | `PdfMemorySignService`, teste de parsing `CMSSignedData` |
| Alegacoes criptograficas | A pagina e os metadados do PDF identificam a assinatura como demonstrativa e dizem explicitamente que nao e ICP-Brasil. | `PdfMemorySignService` |
| Minimizacao no PDF | Nome, e-mail e CPF nao sao mais inseridos na pagina, assinatura visual ou metadados do PDF. O PDF usa somente codigo opaco de verificacao. | `PdfMemorySignService` |
| Upload | A API rejeita arquivo vazio, MIME diferente de PDF, bytes sem cabecalho `%PDF-`, PDF corrompido, sem paginas ou protegido por senha antes da assinatura. | `PdfValidationService`, `PdfSignController` |
| Rotas | Rotas que nao estejam explicitamente publicas ou protegidas deixam de receber `permitAll`; passam a exigir autenticacao. | `SecurityConfig` |
| Verificacao publica | O codigo e aleatorio com 256 bits, codificado em Base64 URL-safe (43 caracteres). A resposta publica nao retorna nome, e-mail ou CPF, e o endpoint que recebia CPF/e-mail para confirmar identidade foi removido. | `PdfSignController`, `VerificationController`, `VerifyPage` |
| Abuso | Login, verificacao e assinatura possuem limite em memoria por IP e janela de um minuto. Os limites sao configuraveis por ambiente. | `RateLimitService`, `application.yml` |
| Dados no navegador | CPF deixa de ser retornado por cadastro/login e deixa de ser guardado pelo frontend. JWT e dados do usuario permanecem apenas na memoria da pagina; atualizar a pagina exige novo login. | `AuthController`, `api.ts`, `App.tsx` |
| Segredos | Banco e JWT nao possuem mais valores padrao no backend ou no Compose. A execucao Docker exige `POSTGRES_PASSWORD` e `JWT_SECRET` em `.env`; foi incluido `.env.example` sem segredos. | `application.yml`, `docker-compose.yml`, `.env.example` |
| Logs e exportacao | Detalhes de auditoria passam por redacao basica de e-mail/CPF; falhas de login nao registram o e-mail informado; erros HTTP nao devolvem a mensagem interna; CSV neutraliza celulas iniciadas por `=`, `+`, `-` ou `@`. | `AuditService`, `AuthController`, `ApiExceptionHandler`, `AdminController` |
| Auditoria de IP | O valor de `X-Forwarded-For` nao e mais aceito sem configuracao de proxy confiavel; por padrao registra-se o IP observado pelo servidor. | `RequestMetadataService` |
| Tempo | Novos registros de usuario, assinatura, revogacao, bloqueio e auditoria passam a ser gravados em UTC. | entidades e controllers correspondentes |

## Testes executados

Em 19/08/2026 foi executado:

```bash
cd DigitalSignature
mvn clean test
```

Resultado: **6 testes, 0 falhas e 0 erros**. A suite agora cobre tambem rota futura bloqueada, rejeicao de bytes falsos com MIME `application/pdf`, ausencia de CPF na resposta de autenticacao, ausencia de dados pessoais na verificacao publica, codigo opaco longo e parsing da assinatura como CMS.

## Revisao necessaria antes de producao

1. **TLS e proxy confiavel:** configurar HTTPS no ambiente e, se necessario, uma lista explicita de proxies que podem encaminhar o IP original. O rate limit atual e local ao processo e nao substitui uma solucao compartilhada para multiplas instancias.
2. **Identidade e assinatura:** o certificado atual e efemero e autoassinado. Para identidade externa, nao repudio ou alegacao ICP-Brasil, e necessario definir a finalidade e implantar certificado, custodia de chave, cadeia, revogacao, carimbo do tempo e validacao independente.
3. **Retencao e direitos do titular:** definir finalidade, base legal aprovada, retencao, descarte, backups e fluxos aplicaveis de acesso/correcao/eliminacao. Estes pontos dependem de decisao institucional e juridica.
4. **Dados em repouso:** decidir e documentar criptografia de volume, banco, backups e, se justificado pelo modelo de ameaca, campos da aplicacao com gestao e rotacao de chaves fora do repositorio.
5. **Schema e migracoes:** substituir `ddl-auto: update` por migracoes versionadas antes de qualquer ambiente compartilhado. A retirada futura de telefone/data de nascimento precisa de migracao e decisao de finalidade; esses dados ainda existem na tabela atual.
6. **Validacao de PDF avancada:** o parser reduz a aceitacao de arquivos falsos/corrompidos, mas nao equivale a uma sandbox completa. Avaliar limites de paginas, tempo de processamento, recursos ativos/anexos e isolamento operacional conforme risco.
7. **Autenticacao do navegador:** o token em memoria reduz persistencia, mas nao protege uma pagina comprometida por XSS. Se o risco justificar, avaliar cookie `HttpOnly` com desenho de CSRF, CSP, renovacao/revogacao de sessao e logout no servidor.
8. **Auditoria:** a redacao cobre padroes comuns, nao toda PII. Definir catalogo fechado de eventos, acesso minimo, retencao e monitoramento; revisar tambem exportacoes CSV e limites de quantidade.
9. **Dependencias e CI:** adicionar scans de segredos e dependencias, junto de `mvn test`, `npm test`, lint e build, como gates de pull request.
10. **Skills e agentes:** as skills locais em `.codex/skills/` continuam como templates TODO e `.codex/AGENTS.md` aponta para nomes de documentos que nao existem na branch. Transformar as skills em procedimentos versionados e corrigir essas referencias antes de delegar revisoes de seguranca a agentes.

## Impactos de contrato

- `POST /v1/verify/{code}` foi removido. A verificacao publica e somente `GET` e retorna estado, data UTC e mensagem minima.
- `POST /v1/auth/register` e `POST /v1/auth/login` nao retornam mais CPF.
- O formato de `X-Verification-Code` mudou de UUID truncado para token Base64 URL-safe de 43 caracteres. Clientes e scripts Postman devem atualizar a validacao.
- A assinatura PDF continua demonstrativa: ela e estruturalmente CMS, mas nao possui cadeia de confianca persistente.
