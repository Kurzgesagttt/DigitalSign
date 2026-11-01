# Instrucoes Para O Copilot

Este projeto deve seguir estas diretrizes em toda alteracao de codigo.

## Versoes E Dependencias

- Seja consistente com as versoes de Java, Maven, Node, bibliotecas e APIs ja adotadas no projeto.
- Nao introduza dependencias duplicadas ou alternativas para resolver o mesmo problema sem justificativa tecnica clara.
- Antes de atualizar versoes, avalie compatibilidade com backend, frontend, Docker e ambiente de execucao.

## Arquitetura E Qualidade De Codigo

- Prefira codigo modular, com responsabilidades bem separadas e baixo acoplamento.
- Estruture a logica para facilitar testes unitarios, evitando regras de negocio acopladas a controladores, componentes de interface ou detalhes de infraestrutura.
- Priorize funcoes e classes pequenas, com contratos claros, nomes descritivos e dependencias injetaveis.
- Sempre que possivel, mantenha regras de negocio em servicos e componentes reutilizaveis.

## Testabilidade

- Implemente codigo que possa ser validado por testes unitarios de forma simples e isolada.
- Evite dependencias estaticas, efeitos colaterais desnecessarios e acesso direto a recursos externos quando isso dificultar mocks ou stubs.
- Ao alterar comportamento relevante, considere o impacto em testes existentes e adicione cobertura quando fizer sentido.

## LGPD

- Respeite e aplique os principios da LGPD em todo fluxo que trate dados pessoais.
- Colete apenas os dados estritamente necessarios para a funcionalidade.
- Evite expor dados pessoais em logs, mensagens de erro, respostas de API, QR codes ou documentos assinados.
- Prefira mascaramento, minimizacao de dados e separacao clara entre dados sensiveis e dados operacionais.
- Considere consentimento, finalidade de uso, retencao minima e exclusao segura quando aplicavel.

## Seguranca Da Aplicacao

- Mantenha a seguranca como requisito padrao, nao como ajuste posterior.
- Nunca exponha chaves, certificados, tokens, senhas ou segredos no codigo-fonte, logs, commits ou exemplos.
- Valide entradas, sanitize dados externos e trate uploads de arquivos com cuidado.
- Evite confiar em dados vindos do cliente sem validacao no backend.
- Prefira configuracoes seguras por padrao e siga o principio do menor privilegio.
- Em funcionalidades de autenticacao, assinatura digital e verificacao, priorize integridade, rastreabilidade e protecao contra manipulacao indevida.

## Mudancas Esperadas

- Preserve compatibilidade com a estrutura atual do projeto sempre que possivel.
- Evite refatoracoes amplas sem necessidade direta.
- Documente decisoes importantes quando a mudanca afetar seguranca, privacidade, arquitetura ou versoes.