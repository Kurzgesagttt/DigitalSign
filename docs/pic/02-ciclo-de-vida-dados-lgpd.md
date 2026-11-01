# Ciclo de vida dos dados e relacao com a LGPD

## Inventario de dados

| Dado ou ativo | Possivel dado pessoal? | Origem | Finalidade tecnica observada | Operacao de tratamento | Local de processamento/armazenamento | Compartilhamento | Retencao observada | Protecao atual | Lacuna |
|---|---|---|---|---|---|---|---|---|---|
| Nome completo | Sim | Cadastro frontend/API | Identificar usuario e signatario | Coleta, armazenamento, uso, exibicao | `usuarios`, `signature_records`, PDF assinado | Publico parcial via verificacao; admin | Indefinida | JWT/RBAC para areas privadas; PDF inclui valor completo | Necessidade e retencao nao justificadas |
| Email | Sim | Cadastro/login/verificacao | Login, identificacao e confirmacao | Coleta, armazenamento, comparacao, exibicao | Banco, PDF, `localStorage` | Publico em `/v1/verify/{code}`; admin; CSV | Indefinida | Unicidade, normalizacao, RBAC admin | Exposicao publica de email completo |
| Telefone | Sim | Cadastro | Campo de usuario | Coleta, armazenamento | `usuarios` | Admin indiretamente se exposto futuramente | Indefinida | Nao retornado nas respostas observadas | Necessidade tecnica nao demonstrada |
| CPF | Sim, sensivel no contexto de risco | Cadastro/verificacao | Identificacao do signatario | Coleta, armazenamento, comparacao, exibicao mascarada/parcial | `usuarios`, `signature_records`, PDF, `localStorage` | PDF contem CPF completo; verificacao mascara; confirmacao exige CPF | Indefinida | Mascara em algumas respostas; unicidade | CPF completo em PDF/metadados e `localStorage`; sem retencao |
| Data de nascimento | Sim | Cadastro | Campo de usuario | Coleta, armazenamento | `usuarios` | Nao observado em respostas | Indefinida | Nao retornada nas respostas lidas | Necessidade tecnica nao demonstrada |
| Senha | Sim/segredo | Cadastro/login | Autenticacao | Coleta, hash, comparacao | `usuarios` como hash | Nao compartilhada | Indefinida | BCrypt para novas senhas; migracao legado | Sem politica de senha, rate limiting ou MFA |
| JWT | Dado de autenticacao | Backend | Autorizacao stateless | Geracao, armazenamento local, envio | `localStorage`, header HTTP | Enviado ao backend | Ate expiracao/logout | Assinado por HMAC; expiracao | `localStorage` exposto a XSS; segredo dev versionado |
| PDF original | Pode conter dados pessoais | Upload | Base para assinatura | Recepcao, leitura, hash, assinatura | Memoria do backend | Nao observado persistido | Durante requisicao | Limite 10 MB; autenticacao | Validacao fraca; sem sanitizacao; sem limpeza de buffers |
| PDF assinado | Pode conter dados pessoais | Backend | Devolucao do documento assinado | Geracao, assinatura, retorno | Memoria; navegador/download | Usuario recebe arquivo | Fora do controle da API apos download | Hash persistido; assinatura | PDF inclui dados pessoais e nao e criptografado |
| Hashes dos PDFs | Dado tecnico, pode ser identificador indireto | Backend | Evidencia de integridade | Calculo, armazenamento, exportacao | `signature_records`, CSV admin | Admin | Indefinida | SHA-256 | Retencao indefinida; nao prova ausencia de dados no PDF |
| Codigo de verificacao | Identificador do registro | Backend | Verificacao publica por QR Code | Geracao, armazenamento, consulta | `signature_records`, PDF/QR | Publico a quem tem o PDF/link | Indefinida | UUID truncado a 8 caracteres | Entropia reduzida e endpoint publico sem rate limiting |
| IP e user-agent | Dados pessoais/identificadores online | Requisicao | Auditoria e rastreabilidade | Coleta, armazenamento, exportacao | `signature_records`, `audit_logs` | Admin/CSV | Indefinida | Truncamento de user-agent | Sem base/finalidade/retencao documentadas |
| Logs de auditoria | Podem conter dados pessoais | Backend | Auditoria operacional | Registro, consulta, exportacao | `audit_logs` | Admin/CSV | Indefinida | RBAC admin; truncamento de detalhes | Detalhes podem conter email/codigo/motivo livre |
| Segredos de ambiente | Segredo tecnico | `.env`, YAML, Compose | Conexao DB/JWT/dev | Armazenamento/configuracao | Arquivos versionados e ambiente | Quem acessa repo | Indefinida | Variaveis de ambiente aceitas | Valores padrao/dev versionados; requer rotacao |

## Principios da LGPD

| Principio | Relacao com o fluxo da API | Evidencia existente | Lacuna | Recomendacao | Como comprovar |
|---|---|---|---|---|---|
| Finalidade | Dados devem ter proposito claro no cadastro, assinatura e verificacao | Campos coletados em `RegisterRequest` e usados parcialmente | Nao ha documento de finalidade/base legal no repo | Definir finalidade por dado e fluxo no PIC/README/API | Tabela de inventario aprovada pelo orientador/controlador |
| Adequacao | Dados devem ser compativeis com o objetivo do prototipo | Nome/email/CPF usados para assinatura/verificacao | Telefone e data de nascimento nao tem uso tecnico observado | Reduzir ou justificar campos | Teste de cadastro sem campos nao necessarios ou justificativa documentada |
| Necessidade | Minimizar coleta e retencao | PDF nao e persistido completo no banco | CPF completo vai para PDF/metadados e `localStorage`; retencao indefinida | Mascarar/reduzir dados no PDF e remover CPF do armazenamento frontend | Testes de resposta/localStorage/PDF metadata |
| Livre acesso | Titular deveria consultar tratamento | API nao possui endpoints de consulta/exportacao para o proprio titular | Apenas admin acessa dados consolidados | Definir fluxo de consulta/correcao/exclusao quando aplicavel | Casos de teste/API e politica documentada |
| Qualidade dos dados | Dados devem ser exatos e atualizados | Normalizacao basica de email, CPF e telefone | Nao ha atualizacao/correcao pelo usuario | Criar processo ou registrar fora do escopo | Evidencia de endpoint/processo |
| Transparencia | Usuario precisa entender uso dos dados | README descreve fluxo; UI mostra assinatura como usuario | Nao ha aviso de privacidade/termos | Adicionar aviso claro sobre dados no PDF, logs e verificacao publica | Capturas/telas/documento revisado |
| Seguranca | Medidas tecnicas para proteger dados | JWT, BCrypt, RBAC admin, limite 10 MB, CORS parametrizado | Sem TLS, criptografia em repouso, rate limiting, validacao robusta de PDF | Implementar backlog P0/P1 | Testes negativos e configuracoes |
| Prevencao | Evitar incidentes antes que ocorram | Tratamento generico de erro em assinatura; auditoria | Falta sanitizacao PDF, monitoramento, protecao de segredos | Validacao, logs seguros, secret scanning, dependencias | Relatorios de testes e scans |
| Nao discriminacao | Evitar tratamento discriminatorio abusivo | Nao ha decisao automatizada observada | Coleta de data nascimento/CPF aumenta risco sem uso claro | Minimizar dados e documentar ausencia de perfilamento | Revisao de fluxo e codigo |
| Responsabilizacao/prestacao de contas | Demonstrar medidas adotadas | `AuditLog`, testes backend/frontend, docs geradas | Sem politica de retencao, resposta a incidente e matriz formal | Manter diario de evidencias e criterios de aceite | `11-diario-de-evidencias.md` atualizado |

## Art. 46 da LGPD

O art. 46 exige medidas de seguranca, tecnicas e administrativas aptas a proteger dados pessoais contra acessos nao autorizados e situacoes acidentais ou ilicitas. No codigo, ha controles tecnicos parciais: autenticacao JWT, autorizacao por role, BCrypt, limite multipart, auditoria e hashes. Nao foi possivel confirmar no repositorio medidas administrativas, politica de retencao, processo de resposta a incidente, gestao de chaves/segredos em ambiente real, TLS em producao ou criptografia em repouso.

Portanto, o prototipo possui controles que podem contribuir para seguranca e prevencao, mas nao comprova conformidade LGPD de forma isolada.
