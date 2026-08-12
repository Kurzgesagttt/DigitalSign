# Conteudo-base para relatorio final

## 1. Introducao e delimitacao do problema

Aplicacoes que processam documentos digitais podem receber arquivos PDF contendo dados pessoais, credenciais de autenticacao e metadados capazes de identificar titulares. No prototipo DigitalSign, o fluxo atual recebe PDFs de usuarios autenticados, processa o arquivo no backend Spring Boot, adiciona dados do signatario ao documento e devolve um PDF assinado. O sistema tambem persiste usuarios, registros de assinatura e logs de auditoria.

Problema de pesquisa sugerido: **Como a aplicacao de praticas de programacao segura e mecanismos criptograficos pode reduzir os riscos de vazamento de dados pessoais durante o processamento de documentos PDF por uma API?**

## 2. Objetivo geral

Analisar e aplicar praticas de desenvolvimento seguro em uma API de documentos digitais, demonstrando como controles de acesso, minimizacao de dados, validacao de arquivos, protecao de chaves, criptografia, assinatura digital e descarte seguro podem reduzir riscos de vazamento e contribuir para os principios de seguranca, prevencao, necessidade e responsabilizacao previstos na LGPD.

## 3. Objetivos especificos

- Identificar os dados e ativos tratados durante envio, processamento e devolucao dos documentos.
- Mapear ameacas de vazamento em cada etapa do fluxo.
- Avaliar e implementar validacao segura de PDFs e limites contra abuso.
- Analisar a protecao dos documentos em transito, processamento e repouso.
- Avaliar protecao de chaves criptograficas e segredos.
- Minimizar coleta, retencao e registro de dados pessoais em logs.
- Avaliar autenticacao e autorizacao conforme o fluxo real.
- Executar testes de seguranca e reunir evidencias antes/depois.
- Relacionar resultados aos principios da LGPD e ao art. 46.
- Esclarecer o papel e as limitacoes da assinatura digital.

## 4. Fundamentacao sobre LGPD e desenvolvimento seguro

A LGPD estabelece principios como finalidade, adequacao, necessidade, transparencia, seguranca, prevencao e responsabilizacao. No contexto de uma API de documentos digitais, esses principios exigem que cada dado coletado tenha proposito documentado, que o tratamento seja limitado ao necessario e que sejam adotadas medidas tecnicas e administrativas para reduzir riscos.

[PENDENTE: incluir revisao juridica/academica sobre base legal, papeis de controlador/operador, titulares e contexto institucional.]

## 5. Fundamentacao criptografica

Assinatura digital, hash criptografico, criptografia de conteudo e TLS fornecem propriedades diferentes. No prototipo, a assinatura PDF contribui para integridade e autenticidade tecnica, mas nao fornece confidencialidade. O hash SHA-256 permite detectar alteracoes por comparacao, mas tambem nao protege o conteudo contra leitura. A confidencialidade em transito dependeria de HTTPS/TLS, que nao foi configurado no repositorio analisado. A confidencialidade em repouso nao foi observada no banco ou nos arquivos.

## 6. Metodologia

A metodologia consistiu em inspecao estatica do repositorio, leitura de controllers, services, models, repositories, configuracoes, frontend, Dockerfiles e testes. Tambem foram executados testes automatizados e comandos de inventario: `mvn test`, `npm test`, `npm run lint`, `npm run build`, `npm audit --audit-level=high` e `mvn dependency:tree -Dscope=compile`.

## 7. Arquitetura e fluxo de dados

O backend usa Spring Boot, Spring Security, JWT, JPA e PostgreSQL. O frontend usa React/Vite e Axios. O usuario se cadastra com nome, email, telefone, CPF, data de nascimento e senha. A senha e armazenada com BCrypt para novos cadastros. No login, o backend retorna JWT, que o frontend armazena em `localStorage`. O PDF e enviado por multipart, processado em memoria, assinado com chave RSA gerada em memoria, recebe pagina de verificacao e QR Code, e retorna ao navegador.

## 8. Resultados observados

Foram observados controles ja implementados: autenticacao JWT, RBAC para admin, BCrypt, limite multipart de 10 MB, auditoria em banco, hashes SHA-256, verificacao publica por codigo e testes automatizados basicos. Tambem foram observadas lacunas relevantes: ausencia de TLS no repo, ausencia de criptografia em repouso, validacao de PDF insuficiente, segredos dev versionados, retencao indefinida, token em `localStorage` e exposicao de dados pessoais no PDF assinado.

## 9. Analise de riscos

Os riscos mais relevantes envolvem interceptacao de dados em transito, upload malicioso, segredos versionados, retencao excessiva, enumeracao/tentativas de verificacao publica e exposicao de CPF no PDF. Esses riscos afetam principalmente confidencialidade, seguranca, prevencao e necessidade.

## 10. Discussao

O prototipo ja demonstra controles importantes para um ambiente academico, mas a assinatura digital nao deve ser tratada como solucao unica de protecao de dados. A assinatura protege integridade/autenticidade tecnica, enquanto confidencialidade exige TLS, criptografia de conteudo/repouso e controles de acesso. A LGPD exige tambem governanca, finalidade, retencao e responsabilizacao, que nao podem ser provadas apenas pela existencia de algoritmos criptograficos.

## 11. Limitacoes

- Nao foi possivel confirmar ambiente de producao, TLS externo ou politicas institucionais.
- Nao foi executada analise Maven de vulnerabilidades com OWASP Dependency-Check.
- Nao foram usados documentos reais nem validacao manual no Adobe Acrobat.
- A analise reflete uma working tree com alteracoes nao commitadas.

## 12. Trabalhos futuros

Implementar validacao robusta de PDF, TLS demonstravel, remocao de segredos do repositorio, minimizacao de dados no PDF e frontend, rate limiting, logs mascarados, retencao/descarte e scans de seguranca no CI. Avaliar criptografia em repouso e PKI real apenas se forem necessarias ao objetivo do prototipo.

## 13. Conclusao

O DigitalSign possui uma base funcional para estudar protecao de dados em APIs de documentos digitais, mas ainda apresenta lacunas importantes para uma demonstracao segura. A evolucao deve priorizar controles demonstraveis e proporcionais: validacao de upload, transporte seguro, gestao de segredos, minimizacao, logs seguros, retencao e testes negativos. A assinatura digital deve ser apresentada como um dos controles de integridade/autenticidade, e nao como garantia isolada de conformidade com a LGPD.
