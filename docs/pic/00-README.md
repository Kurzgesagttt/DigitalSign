# Documentacao PIC - protecao de dados em APIs de documentos digitais

## Objetivo

Esta pasta registra uma analise tecnica do prototipo DigitalSign para apoiar o novo recorte do PIC: **Protecao de dados pessoais em APIs de documentos digitais: aplicacao da LGPD, programacao segura e criptografia**.

A documentacao separa o que foi observado no codigo do que e recomendacao de evolucao. Nenhum controle tecnico isolado deve ser interpretado como conformidade automatica com a LGPD; a avaliacao juridica, a definicao de finalidade, base legal, retencao e papeis dos agentes de tratamento dependem do contexto institucional.

## Escopo analisado

- Backend Spring Boot em `DigitalSignature/`.
- Frontend React/Vite em `DigitalSignatureFront/`.
- Configuracoes em `application.yml`, `.env`, `docker-compose.yml`, Dockerfiles e Nginx.
- Testes automatizados backend/frontend existentes.
- Working tree atual, incluindo alteracoes nao commitadas.

Nao foram alterados arquivos de codigo-fonte nesta etapa.

## Identificacao da analise

| Campo | Valor |
|---|---|
| Data da analise | 2026-08-07 |
| Fuso informado | America/Sao_Paulo |
| Branch | `main` |
| Commit base informado por Git | `b1e786bb6dc3a62c57b618f738ef5a81609d1c86` |
| Estado da arvore | Working tree com alteracoes modificadas e arquivos nao rastreados |
| Ambiente | Windows, PowerShell, Java 17, Node/npm ja instalados |

## Ordem de leitura

1. `01-estado-atual-da-api.md`
2. `02-ciclo-de-vida-dados-lgpd.md`
3. `03-analise-programacao-segura.md`
4. `04-analise-criptografia.md`
5. `05-modelagem-ameacas-e-riscos.md`
6. `06-matriz-rastreabilidade-pic.md`
7. `07-plano-de-implementacao.md`
8. `08-plano-de-testes-e-evidencias.md`
9. `09-conteudo-base-relatorio-final.md`
10. `10-referencias.md`
11. `11-diario-de-evidencias.md`

## Distincoes conceituais usadas

| Mecanismo | Contribuicao principal |
|---|---|
| Assinatura digital | Integridade, autenticidade e verificacao de autoria |
| Hash criptografico | Deteccao de alteracao e apoio a verificacao de integridade |
| Criptografia do conteudo/arquivo | Confidencialidade dos dados |
| HTTPS/TLS | Confidencialidade e integridade dos dados em transito |
| Autenticacao e autorizacao | Controle de quem pode executar operacoes e acessar documentos |
| Retencao e descarte seguro | Reducao da exposicao de dados apos o fim da necessidade |
