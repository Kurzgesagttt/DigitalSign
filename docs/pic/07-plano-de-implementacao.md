# Plano de implementacao priorizado

## Backlog

| Prioridade | Melhoria | Risco tratado | Relacao com LGPD | Componentes afetados | Criterio de aceite | Testes necessarios | Complexidade estimada |
|---|---|---|---|---|---|---|---|
| P0 | Remover segredos reais/padroes sensiveis do versionamento e criar `.env.example` | R3/R6 | Seguranca, prevencao | Config, Compose, docs | Repo sem segredo operacional; app exige segredo forte em prod | Secret scan; start local com `.env` | Media |
| P0 | Configurar TLS ou documentar terminacao obrigatoria | R1 | Seguranca, prevencao | Nginx/infra/backend docs | Ambiente demonstravel com HTTPS | Teste TLS e captura | Media |
| P0 | Validacao robusta de PDF | R2/R11 | Seguranca, necessidade | `PdfSignController`, servico de validacao | Rejeita vazio, falso, MIME errado, corrompido e acima do limite | Testes negativos MockMvc | Media |
| P0 | Negar/autenticar rotas por padrao | R12 | Seguranca | `SecurityConfig` | Rotas nao listadas nao ficam publicas | Teste endpoint dummy/unknown | Baixa |
| P0 | Reduzir dados pessoais no PDF e `localStorage` | R7/R9 | Necessidade, seguranca | `PdfMemorySignService`, frontend | CPF nao aparece completo sem justificativa; frontend nao guarda CPF | Teste localStorage e metadados PDF | Media |
| P1 | Rate limiting para login, verificacao e assinatura | R5/R11 | Prevencao | Backend/filter/proxy | Limites por IP/usuario configurados | Testes de exceder limite | Media |
| P1 | Padronizar erros e logs/auditoria mascarados | R10 | Seguranca, responsabilizacao | `ApiExceptionHandler`, `AuditService`, controllers | Sem stack trace/dados sensiveis nas respostas/logs | Testes de erro e auditoria | Media |
| P1 | Politica de retencao e job de descarte | R4 | Necessidade, prevencao | Entidades, scheduler, docs | Dados expirados removidos/anonimizados conforme politica | Teste com registros antigos | Media |
| P1 | Testes automatizados de seguranca | Todos | Responsabilizacao | Backend/frontend CI | Suite cobre casos negativos definidos | `mvn test`, `npm test` | Media |
| P1 | Scan de dependencias Maven e npm no CI | Dependencias vulneraveis | Prevencao | CI/build | Relatorio gerado sem achados criticos | Dependency-Check/npm audit | Media |
| P2 | Criptografia em repouso de campos/volume | R6 | Seguranca | Banco/infra/app | Chaves fora do repo; dados protegidos em repouso | Teste de acesso ao dump/volume | Alta |
| P2 | Fluxo de direitos do titular | Livre acesso/qualidade | Livre acesso, transparencia | API/UI/processo | Consulta/correcao/exclusao documentadas | Testes funcionais | Alta |
| P2 | PKI/certificado real ou validacao externa | Limitacao de assinatura | Responsabilizacao, autenticidade | Servico de assinatura | Certificado/cadeia documentados se o objetivo exigir | Validacao Adobe/cadeia | Alta |
| P2 | Monitoramento e resposta a incidentes | R10/R11 | Prevencao, responsabilizacao | Observabilidade/infra | Alertas para abuso/falhas | Simulacao de incidente | Media |

## Ordem recomendada

1. Segredos e configuracao segura.
2. Validacao de PDF e testes negativos.
3. TLS demonstravel.
4. Minimizacao de CPF/dados no PDF e frontend.
5. Logs/erros/rate limiting.
6. Retencao/descarte.
7. Scans e automacao CI.
