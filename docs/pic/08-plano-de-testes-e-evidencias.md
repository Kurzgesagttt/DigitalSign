# Plano de testes e evidencias

Todos os testes devem usar PDFs sinteticos e dados ficticios. Nao usar CPF, email, documentos ou nomes reais.

| Teste | Objetivo | Pre-condicao | Passos/comando | Resultado esperado | Evidencia a capturar | Cuidado |
|---|---|---|---|---|---|---|
| PDF valido | Confirmar assinatura funcional | Usuario autenticado | `mvn test` ou MockMvc multipart com PDFBox | `200`, PDF, codigo verificacao, hashes salvos | Log do teste e registro `SignatureRecord` | PDF sem dados reais |
| Extensao `.pdf` com conteudo falso | Rejeitar arquivo falso | Validador robusto implementado | Enviar bytes `hello` com nome `.pdf` | `400` seguro | Teste automatizado | Nao depender so de nome |
| MIME type incorreto | Rejeitar tipo incorreto | API rodando | Enviar PDF valido com `text/plain` | `400` | MockMvc/curl | Registrar diferenca de MIME e conteudo |
| Arquivo vazio | Evitar processamento vazio | API rodando | Multipart vazio | `400` | Teste MockMvc | N/A |
| PDF corrompido | Evitar erro interno | API rodando | Enviar `%PDF-` incompleto | `400`, sem stack trace | Resposta e log | N/A |
| Acima do limite | Validar 10 MB ou limite configurado | Limite definido | Enviar arquivo acima do limite | `413` ou `400` padronizado | Teste automatizado | Nao criar arquivo enorme no repo |
| Acesso nao autenticado | Garantir protecao | Sem JWT | `POST /v1/sign` | `401/403` | Teste MockMvc | N/A |
| Acesso nao autorizado | Garantir RBAC | JWT USER | `GET /v1/admin/users` | `403` | Teste existente/novo | N/A |
| IDOR/BOLA | Garantir escopo por objeto | Se houver documentos por usuario | Usuario A tenta acessar objeto B | `403/404` | Teste | Aplicavel somente se existir endpoint por objeto |
| Logs sem conteudo pessoal | Evitar vazamento | Eventos catalogados | Executar falhas com CPF/email ficticios | Logs nao contem CPF/email indevidos | Consulta `audit_logs` | Dados ficticios |
| Descarte de temporarios | Confirmar limpeza | Se houver temp files | Processar PDF e inspecionar diretorio temp | Nenhum PDF residual | Listagem do diretorio | Nao expor arquivo real |
| Erro interno seguro | Evitar stack trace publico | Simular excecao | Forcar falha controlada | Resposta generica com correlacao | Response/body/log | Nao vazar excecao |
| TLS | Confirmar HTTPS | Ambiente TLS | `curl -vk https://host/actuator/health` | Certificado e negociacao TLS | Saida do curl | Usar ambiente de teste |
| Segredos | Detectar segredos | Scanner configurado | `gitleaks detect` ou equivalente | Sem achados reais | Relatorio | Mascarar qualquer achado |
| Dependencias | Detectar vulnerabilidades | Ferramenta instalada | `npm audit --audit-level=high`; Dependency-Check Maven | Sem vulnerabilidades altas/criticas | Saida/relatorio | Atualizar base antes de concluir |
| Adobe Acrobat | Diferenciar estrutura e confianca | PDF assinado gerado | Abrir/validar assinatura | Acrobat reconhece estrutura; confianca depende de certificado | Captura textual/manual | Nao afirmar ICP-Brasil |
| Antes/depois | Evidenciar melhoria | Baseline salvo | Rodar teste antes e depois da mudanca | Falha vira sucesso ou risco reduzido | Diario de evidencias | Mesmo dado ficticio |

## Evidencias ja coletadas

- `mvn test`: sucesso, 5 testes backend.
- `npm test`: sucesso com permissao elevada, 3 testes frontend.
- `npm run lint`: sucesso.
- `npm run build`: sucesso com permissao elevada.
- `npm audit --audit-level=high`: 0 vulnerabilidades reportadas.
- `mvn dependency:tree -Dscope=compile`: inventario Maven gerado com sucesso.
