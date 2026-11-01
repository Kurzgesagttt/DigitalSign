# Instruções para agentes

Estas instruções valem para todo o repositório.

Antes de alterar código, leia `README.md` e os documentos em `.codex/`: `README.md`, `architecture.md`, `lgpd-security.md` e, para autenticação, assinatura, hash, chaves, certificados, tokens ou códigos de verificação, `cryptography.md`.

## Regras inegociáveis

- Trate PDFs, CPF, nome, e-mail, telefone, data de nascimento, credenciais, IP e identificadores persistentes como dados potencialmente pessoais.
- Não use, copie, gere ou versione dados pessoais reais. Testes e exemplos usam dados sintéticos inequivocamente fictícios.
- Não exponha dados pessoais, segredos, documentos ou conteúdo de requisições em logs, erros, URLs, QR Codes, telemetria ou fixtures.
- Não escolha base legal, prazo de retenção ou finalidade por conta própria. Se não estiverem documentados, registre a lacuna e solicite decisão do responsável pela pesquisa.
- Minimize coleta e respostas. O backend obtém a identidade do contexto autenticado; nunca confie em nome, e-mail ou CPF enviados pelo cliente para autorizar uma assinatura.
- Não crie algoritmos criptográficos nem protocolos próprios. Use bibliotecas consolidadas, primitivas atuais e gestão explícita do ciclo de vida das chaves.
- Senhas usam função adaptativa específica (`Argon2id`, preferencialmente, ou `bcrypt`/`scrypt`) com salt; SHA-256 simples não é armazenamento aceitável.
- Não anuncie uma assinatura como ICP-Brasil, qualificada, legalmente equivalente ou verificável a longo prazo sem comprovação técnica e documental.
- Mudanças incluem testes proporcionais ao risco e não reduzem controles sem justificativa escrita.
- Nunca alegue que uma alteração “garante conformidade com a LGPD”. Relate controles, riscos residuais e decisões pendentes de validação jurídica/institucional.

## Forma de trabalhar

- Preserve alterações existentes e mantenha o escopo da tarefa.
- Prefira mudanças pequenas, coesas e reversíveis.
- Antes de codar, identifique dados envolvidos, fronteiras de confiança e efeitos sobre confidencialidade, integridade, disponibilidade e direitos dos titulares.
- Ao terminar, execute os comandos aplicáveis de `.codex/README.md` e relate testes não executados.
- Toda dependência nova precisa de necessidade clara, manutenção ativa, licença compatível e avaliação de segurança.
