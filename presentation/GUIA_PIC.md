# Guia da demonstração do PIC

## Objetivo

Apresentar a API DigitalSign como estudo de caso para investigar como programação segura e criptografia podem reduzir riscos de exposição de dados pessoais no processamento de PDFs. A demonstração evidencia etapas do fluxo, controles observáveis e limites do protótipo. Ela não certifica conformidade jurídica com a LGPD nem validação ICP-Brasil.

## Preparação

- Inicie a API e o Postman no ambiente local.
- Use somente a conta descartável e o PDF sintético preparados para a apresentação. Não carregue documentos reais.
- Carregue o ambiente Postman `DigitalSign - local sintetico`; configure uma senha local de demonstração e, se necessário, um token ADMIN de demonstração. Não grave tokens em capturas ou no relatório.
- Confirme que a conta de demonstração não conflita com dados já existentes. A remoção da conta deve ser a última etapa e somente em dados descartáveis.
- Anote a versão/commit e o ambiente apresentados. O relatório analisa uma versão anterior da working tree; confirme o estado atual antes de atribuir controles à versão exibida.

## Ordem sugerida

1. **Delimitar o estudo.** Explique que a pergunta é como controles de programação segura e criptografia podem reduzir riscos no ciclo de vida de PDFs. Diferencie observação técnica, recomendação e decisão institucional.
2. **Autenticar.** Cadastre ou autentique a conta sintética. Mostre que as operações autenticadas usam token, sem exibir seu valor.
3. **Consultar a própria conta.** Consulte `GET /v1/users/me`. A resposta inclui os dados do usuário autenticado, total de assinaturas, categorias/campos e finalidades declaradas, além de descrição e limites da exclusão; não deve incluir senha/hash. Destaque que a identidade vem do contexto autenticado. Categorias e finalidades devolvidas são declarações técnicas da aplicação; não representam, por si, aprovação institucional ou definição de hipótese legal.
4. **Assinar o PDF sintético.** Envie o arquivo pela requisição `POST /v1/sign` como campo multipart `file`. Mostre que a API devolve um PDF e um código de verificação; não chame o resultado de ICP-Brasil.
5. **Verificar.** Consulte `GET /v1/verify/{code}` e observe o status e a data retornados pela API atual. O código é um identificador opaco de demonstração; não é prova isolada da integridade do arquivo.
6. **Mostrar controles administrativos, se aplicável.** Com conta ADMIN sintética, apresente apenas a função necessária, como paginação, auditoria ou revogação. Não use dados reais nem exporte relatórios com dados de terceiros.
7. **Encerrar com exclusão.** Em conta descartável, demonstre `DELETE /v1/users/me` apenas após concluir as demais etapas, enviando `{ "senha": "…" }`. Sucesso retorna `204` sem corpo; senha ausente retorna `400`, senha incorreta `401`, e limite excedido `429`. A operação remove a conta, assinaturas e auditoria da conta/assinaturas; não remove PDFs já baixados nem backups externos. Mostre esse limite e não descreva a resposta como eliminação universal.

## Pontos de segurança e proteção de dados

- PDFs podem conter dados pessoais; a demonstração usa conteúdo fictício e minimiza o que aparece em capturas.
- Hash, assinatura, criptografia e controle de acesso têm propriedades diferentes. Uma operação não substitui TLS, autorização, minimização, retenção ou gestão de chaves.
- A verificação pública deve ser descrita conforme a resposta observada. Código QR ou de verificação, por si só, não comprova cadeia de confiança nem integridade do PDF.
- O artigo relata controles como BCrypt, JWT/RBAC, hashes, limite de upload e auditoria, e também aponta lacunas. Trate-os como observações do recorte e da versão analisados, não como garantia da versão atual ou da implantação.
- O artigo não comprovou TLS, criptografia em repouso, política de retenção nem cadeia de certificados confiável. Só atualize essas afirmações com evidência da versão e ambiente demonstrados.

## Evidências para capturar

- Identificação da versão/commit e ambiente local, sem expor configurações ou segredos.
- Respostas de cadastro/login e consulta do perfil com valores exclusivamente sintéticos; oculte token e senha.
- Requisição multipart e resultado da assinatura usando o PDF sintético; remova ou mascare o código se a captura for publicada.
- Resposta de verificação, registrando os campos efetivamente devolvidos.
- Para ADMIN: resposta de autorização com papel apropriado, paginação/auditoria ou revogação, sempre em dados sintéticos.
- Para exclusão: status `204` sem corpo, tentativa posterior de consultar a conta, remoção de assinaturas e auditoria sob escopo da API, e limites para PDFs baixados/backups externos.
- Registre método, rota, status, horário e resultado. Não capture cabeçalhos `Authorization`, senhas, CPF, PDFs reais, IPs ou conteúdo de auditoria identificável.

## Resultados observados e decisões do controlador

**Evidência técnica** pode demonstrar comportamento de uma versão: validação de entrada, autenticação/autorização, conteúdo de resposta, processamento, auditoria, exclusão observável ou uma negociação TLS. Apresente cada item com sua evidência e limite.

**Decisões do controlador/instituição** incluem finalidade e hipótese legal, campos necessários, transparência, prazo e evento de retenção, tratamento de backups, destinatários, exceções à exclusão, atendimento a direitos e aprovações ética/institucional. A demonstração não decide esses pontos e não transforma a implementação de um controle em declaração de conformidade.