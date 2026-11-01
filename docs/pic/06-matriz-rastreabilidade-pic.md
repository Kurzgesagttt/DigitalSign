# Matriz de rastreabilidade do PIC

| Objetivo/requisito do PIC | Situacao no codigo | Evidencia | Lacuna | Acao proposta | Evidencia esperada para o relatorio |
|---|---|---|---|---|---|
| HTTPS/TLS | Nao comprovado | `nginx.conf`, `docker-compose.yml`, `application.yml` usam HTTP | Sem certificado/config TLS | Configurar TLS em proxy/ambiente | Captura de teste TLS e config |
| Criptografia em repouso | Nao implementado | Entidades JPA e Compose sem criptografia | Banco/logs sem protecao criptografica observada | Avaliar criptografia de disco/coluna | Config e teste de acesso |
| Hashing | Implementado parcial | `HashService.sha256`; hashes em `SignatureRecord` | Hash nao e usado em endpoint de validacao de arquivo | Criar verificacao comparativa opcional | Teste alteracao de PDF/hash |
| Assinatura assimetrica/PKI | Parcial | RSA 2048 em `KeyGenerator`; `PDSignature` | Sem certificado/cadeia confiavel/ICP-Brasil | Reposicionar como controle tecnico demonstrativo | Inspecao do PDF e texto explicativo |
| React | Implementado | `DigitalSignatureFront/package.json`, `src/` | N/A | Manter testes frontend | `npm test`, `npm run build` |
| Spring Boot | Implementado | `DigitalSignature/pom.xml`, `Application.java` | N/A | Manter testes backend | `mvn test` |
| RBAC | Implementado parcial | `UserRole`, `SecurityConfig`, `AdminController` | Apenas USER/ADMIN; bootstrap por email | Testar escopos e negar por padrao | Testes de acesso admin/user |
| OAuth2 | Nao implementado | Nao ha dependencias/config OAuth2 | PIC nao deve afirmar OAuth2 implementado | Remover promessa ou adicionar requisito futuro | Matriz revisada |
| JWT | Implementado parcial | `JwtService`, `JwtAuthenticationFilter` | Segredo dev versionado; sem revogacao | Secret manager e estrategia de revogacao | Teste token expirado/revogado |
| Isolamento de armazenamento | Parcial | PDF nao persistido; banco comum para usuarios/assinaturas/logs | Sem isolamento por usuario/tenant | Definir necessidade real | Testes BOLA se houver documentos persistidos |
| Logs/auditoria | Implementado parcial | `AuditLog`, `AuditService`, rotas admin | Retencao/mascara incompletas | Catalogo de eventos e sanitizacao | Testes de logs sem dados indevidos |
| Monitoramento | Parcial | Actuator `health,info` | Sem metricas de seguranca/incidentes | Adicionar metricas e alertas minimos | Evidencia de endpoint/alerta |
| Deteccao de incidentes | Nao implementado | Nao ha regras/alertas | Auditoria existe, mas sem deteccao | Definir eventos e alertas | Simulacao documentada |
| Validacao segura de PDF | Parcial | `Content-Type` apenas | Sem validacao real | Implementar P0 | Testes MIME falso/corrompido |
| Protecao de chaves/segredos | Parcial/critico | Valores dev versionados; chave PDF efemera | Sem rotacao/secret manager | Rotacionar e externalizar | Secret scan limpo |
| Retencao/descarte | Nao implementado | Sem jobs/politicas | Dados indefinidos no banco | Definir politica e expurgo | Testes de expiracao |
