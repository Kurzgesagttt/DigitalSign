# DigitalSign Backlog

Roadmap tecnico para evoluir o DigitalSign de um assinador funcional para uma aplicacao com autenticacao robusta, roles, painel administrativo, auditoria e controles minimos de seguranca.

## Status da implementacao

- P0, P1 e P2 foram implementados no backend e frontend.
- TASK-016: decisao inicial registrada: assinaturas nao expiram automaticamente; permanecem validas ate revogacao ou marcacao administrativa futura.
- TASK-017: decisao inicial registrada: manter `USER` e `ADMIN` nesta versao; perfis `AUDITOR` e `SUPPORT` ficam para quando houver necessidade operacional real.
- TASK-018: implementado como exportacao CSV administrativa para assinaturas e logs de auditoria, com evento de auditoria registrado a cada exportacao.

## P0 - Base de seguranca

### TASK-001 - Adicionar roles de usuario

**Objetivo:** permitir autorizacao por perfil no backend e no frontend.

**Escopo tecnico:**
- Adicionar campo `role` em `Usuario`.
- Definir os perfis iniciais `USER` e `ADMIN`.
- Garantir que novos cadastros recebam `USER` por padrao.
- Criar uma forma controlada de promover o primeiro admin, por seed, migration, script ou variavel de ambiente.

**Criterios de aceite:**
- Todo usuario salvo possui role valida.
- Cadastro publico nunca cria admin diretamente.
- Login retorna a role necessaria para a interface adaptar a navegacao.

### TASK-002 - Implementar autenticacao real

**Objetivo:** substituir o controle apenas em estado local por autenticacao verificavel pelo servidor.

**Escopo tecnico:**
- Implementar JWT ou sessao server-side.
- Retornar credencial no login.
- Validar a credencial em endpoints protegidos.
- Persistir a credencial no frontend de forma compativel com logout.

**Criterios de aceite:**
- Usuario nao autenticado nao acessa endpoints protegidos.
- Logout remove a credencial do cliente.
- Requisicoes autenticadas enviam a credencial automaticamente.

### TASK-003 - Proteger endpoints sensiveis

**Objetivo:** impedir uso indevido das rotas de assinatura e administracao.

**Escopo tecnico:**
- Exigir autenticacao em `/v1/sign`.
- Exigir role `ADMIN` em todas as rotas administrativas futuras.
- Manter `/v1/auth/register`, `/v1/auth/login` e verificacao publica acessiveis sem login.

**Criterios de aceite:**
- Usuario anonimo nao consegue assinar PDF.
- Usuario `USER` nao consegue acessar rotas admin.
- Rotas publicas de verificacao continuam funcionando via QR Code.

### TASK-004 - Vincular assinatura ao usuario autenticado

**Objetivo:** impedir que o cliente informe manualmente nome, email e CPF de outro assinante.

**Escopo tecnico:**
- Alterar `/v1/sign` para receber apenas o arquivo PDF.
- Obter nome, email e CPF do usuario autenticado no backend.
- Salvar a assinatura associada ao usuario autenticado.
- Ajustar o frontend para parar de enviar dados pessoais no multipart.

**Criterios de aceite:**
- O PDF assinado usa os dados do usuario logado.
- Nao e possivel assinar informando CPF ou email arbitrario.
- O historico de assinatura pode ser associado ao usuario correto.

### TASK-005 - Usar algoritmo seguro para senha

**Objetivo:** substituir hash simples SHA-256 por um mecanismo adequado para senhas.

**Escopo tecnico:**
- Usar BCrypt ou algoritmo equivalente com salt.
- Atualizar cadastro para salvar senha com o novo algoritmo.
- Atualizar login para validar senha usando o verificador do algoritmo.
- Definir estrategia para usuarios ja existentes em ambiente local.

**Criterios de aceite:**
- Senhas novas nao sao armazenadas com SHA-256 simples.
- Login valida corretamente senhas armazenadas com BCrypt.
- Erros de autenticacao continuam genericos.

## P1 - Painel admin

### TASK-006 - Criar painel administrativo no frontend

**Objetivo:** fornecer uma area visual para operacoes administrativas essenciais.

**Escopo tecnico:**
- Criar rota de admin protegida por role.
- Exibir link de admin apenas para usuarios `ADMIN`.
- Incluir telas iniciais para usuarios, assinaturas e metricas.

**Criterios de aceite:**
- Usuario comum nao ve nem acessa a area admin.
- Admin consegue navegar pelas telas principais.
- A interface trata estados de carregamento, erro e lista vazia.

### TASK-007 - Criar endpoints admin para usuarios

**Objetivo:** permitir gestao basica de usuarios cadastrados.

**Escopo tecnico:**
- Criar endpoint para listar usuarios com paginacao ou limite definido.
- Retornar apenas dados necessarios para administracao.
- Evitar exposicao desnecessaria de CPF completo e senha.

**Criterios de aceite:**
- Admin lista usuarios cadastrados.
- A resposta nunca inclui senha.
- Dados sensiveis sao mascarados quando nao houver necessidade operacional.

### TASK-008 - Bloquear e desbloquear usuarios

**Objetivo:** permitir resposta administrativa a abuso, fraude ou solicitacao interna.

**Escopo tecnico:**
- Adicionar status de conta ou flag `bloqueado`.
- Impedir login ou assinatura por usuario bloqueado.
- Registrar quem bloqueou, quando e o motivo.

**Criterios de aceite:**
- Usuario bloqueado nao consegue assinar documentos.
- Admin consegue bloquear e desbloquear usuarios.
- A acao fica registrada para auditoria.

### TASK-009 - Consultar assinaturas no admin

**Objetivo:** permitir investigacao e suporte sem depender apenas do QR Code publico.

**Escopo tecnico:**
- Criar consulta por codigo de verificacao, usuario e intervalo de datas.
- Exibir signatario, data, status e codigo.
- Manter dados sensiveis mascarados por padrao.

**Criterios de aceite:**
- Admin encontra uma assinatura por codigo.
- Admin filtra assinaturas por usuario ou periodo.
- A tela nao permite alterar dados historicos da assinatura.

### TASK-010 - Exibir metricas basicas

**Objetivo:** dar visibilidade operacional simples sobre uso do sistema.

**Escopo tecnico:**
- Mostrar total de usuarios.
- Mostrar total de assinaturas.
- Mostrar assinaturas por usuario ou ranking simples de uso.

**Criterios de aceite:**
- Admin visualiza metricas sem consultar banco manualmente.
- Metricas carregam sem expor dados sensiveis desnecessarios.
- Falha de carregamento e exibida de forma clara.

## P2 - Auditoria e confianca juridica

### TASK-011 - Criar trilha de auditoria

**Objetivo:** registrar eventos relevantes para seguranca, suporte e investigacao.

**Escopo tecnico:**
- Registrar eventos de login, falha de login, assinatura, verificacao e acao administrativa.
- Armazenar usuario, data/hora, tipo do evento, resultado e metadados relevantes.
- Evitar registrar senha, token ou conteudo integral do PDF.

**Criterios de aceite:**
- Eventos criticos geram registro de auditoria.
- Admin consegue consultar eventos relevantes.
- Dados secretos nao aparecem nos logs de auditoria.

### TASK-012 - Registrar metadados da assinatura

**Objetivo:** aumentar rastreabilidade e verificabilidade de cada assinatura.

**Escopo tecnico:**
- Salvar IP e user-agent da requisicao de assinatura.
- Calcular e salvar hash do PDF original.
- Calcular e salvar hash do PDF assinado quando aplicavel.
- Associar assinatura ao usuario autenticado.

**Criterios de aceite:**
- Cada assinatura possui metadados suficientes para auditoria.
- Hashes sao gerados de forma deterministica.
- Metadados aparecem na consulta administrativa.

### TASK-013 - Adicionar status de assinatura

**Objetivo:** permitir ciclo de vida minimo para assinaturas.

**Escopo tecnico:**
- Adicionar status `VALIDA`, `REVOGADA` e `SUSPEITA`.
- Exibir status na verificacao publica e no admin.
- Manter `VALIDA` como status padrao de novas assinaturas.

**Criterios de aceite:**
- Nova assinatura nasce como valida.
- Verificacao publica informa quando uma assinatura nao esta valida.
- Admin consegue filtrar por status.

### TASK-014 - Permitir revogacao administrativa

**Objetivo:** permitir invalidar uma assinatura sem apagar historico.

**Escopo tecnico:**
- Criar acao admin para revogar assinatura.
- Exigir motivo de revogacao.
- Registrar admin responsavel, data/hora e motivo.
- Impedir exclusao fisica da assinatura como mecanismo de revogacao.

**Criterios de aceite:**
- Assinatura revogada continua consultavel.
- Verificacao publica mostra que a assinatura foi revogada.
- Auditoria registra quem revogou e por qual motivo.

### TASK-015 - Mascarar dados sensiveis por padrao

**Objetivo:** reduzir exposicao desnecessaria de CPF e outros dados pessoais.

**Escopo tecnico:**
- Mascarar CPF em listagens e respostas publicas.
- Expor CPF completo apenas onde houver necessidade tecnica clara.
- Evitar retornar telefone e data de nascimento em telas administrativas basicas.

**Criterios de aceite:**
- Verificacao publica nao exibe CPF completo.
- Listagem admin usa dados minimizados.
- Endpoints nao retornam senha, tokens ou dados pessoais sem necessidade.

## P3 - Melhorias futuras

### TASK-016 - Avaliar expiracao de assinatura

**Objetivo:** decidir se assinaturas devem expirar ou permanecer validas indefinidamente.

**Escopo tecnico:**
- Levantar regra de negocio desejada.
- Definir impacto na verificacao publica.
- Planejar migracao caso a expiracao seja adotada.

**Criterios de aceite:**
- Decisao registrada no projeto.
- Caso adotado, existe regra clara de exibicao para assinatura expirada.

### TASK-017 - Avaliar permissoes granulares

**Objetivo:** preparar o sistema para times maiores sem complicar a primeira versao admin.

**Escopo tecnico:**
- Avaliar separacao entre `ADMIN`, `AUDITOR` e `SUPPORT`.
- Definir quais acoes cada perfil pode executar.
- Implementar apenas se houver necessidade real de operacao.

**Criterios de aceite:**
- Decisao documentada antes da implementacao.
- Nenhum perfil novo ganha permissoes amplas por acidente.

### TASK-018 - Criar relatorios exportaveis

**Objetivo:** permitir exportacao controlada de dados administrativos.

**Escopo tecnico:**
- Exportar assinaturas e auditoria em CSV ou PDF.
- Aplicar filtros antes da exportacao.
- Registrar evento de auditoria quando relatorio for exportado.

**Criterios de aceite:**
- Admin exporta relatorio filtrado.
- Exportacao respeita mascaramento ou justificativa de acesso.
- A exportacao fica registrada na auditoria.

## Regras gerais para implementacao

- Priorizar P0 antes de construir o painel admin visual.
- Nao permitir edicao de dados historicos de assinatura.
- Nao apagar fisicamente assinaturas como solucao de suporte.
- Nao afirmar conformidade juridica ou LGPD sem validacao propria.
- Incluir testes proporcionais ao risco de cada task.
