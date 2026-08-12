# Analise criptografica

## Propriedades por mecanismo

| Controle criptografico | Implementacao encontrada | Propriedade fornecida | Dados protegidos | Momento da protecao | Limitacoes | Evidencia |
|---|---|---|---|---|---|---|
| Assinatura PDF | `PDSignature` com `SHA256withRSA` e RSA 2048 gerado em memoria | Integridade/autenticidade tecnica associada a chave usada | PDF assinado | Ao gerar resposta de `/v1/sign` | Chave efemera; sem certificado X.509 confiavel; nao equivale a ICP-Brasil | `PdfMemorySignService`, `KeyGenerator` |
| Hash SHA-256 | Hash do PDF original e assinado salvo no banco | Deteccao de alteracao por comparacao futura | Conteudo dos PDFs como digest | Apos assinatura | Hash nao fornece confidencialidade; nao ha endpoint de comparacao do arquivo | `HashService`, `PdfSignController` |
| JWT HMAC | `jjwt` com segredo configurado e expiracao | Integridade/autenticidade do token | Claims `sub`, `uid`, `role` | Login/cadastro e requisicoes autenticadas | Segredo padrao/dev versionado; sem revogacao de token | `JwtService`, `application.yml` |
| Senhas BCrypt | `BCryptPasswordEncoder`; migracao de SHA-256 legado | Protecao contra exposicao direta da senha | Senha do usuario no banco | Cadastro/login | Sem politica de complexidade/rate limiting | `SecurityConfig`, `AuthController` |
| HTTPS/TLS | Nao configurado no repo | Nao implementado no escopo analisado | Dados em transito | Upload/download/login | Pode existir fora do repo, mas nao foi possivel confirmar | `nginx.conf`, `docker-compose.yml` |
| Criptografia em repouso | Nao observada | Nao implementado | Banco, PDFs, logs | Armazenamento | Depende de infraestrutura externa nao documentada | Entidades/configuracoes |
| Criptografia do PDF | Nao observada | Nao implementado | Conteudo do PDF | Arquivo final/em repouso | PDF e assinado, nao criptografado | `PdfMemorySignService` |

## Assinatura digital vs confidencialidade

O PDF atual e assinado, mas nao criptografado. A assinatura digital contribui para integridade e autenticidade tecnica do documento assinado; ela nao impede leitura do conteudo. Como o PDF assinado inclui nome, email e CPF em pagina adicional e metadados, esses dados permanecem visiveis a quem receber o arquivo.

## Certificado e confianca no Adobe Acrobat

Nao foi encontrado certificado X.509 persistido, keystore, cadeia ICP-Brasil ou integracao com autoridade certificadora. O codigo gera um par RSA em memoria para cada assinatura. Portanto, leitores como Adobe Acrobat podem reconhecer uma estrutura de assinatura PDF, mas nao ha evidencia de que confiem em uma cadeia de certificado valida. Nao se deve descrever a assinatura atual como assinatura ICP-Brasil valida.

## Chaves e segredos

| Item | Observacao | Risco |
|---|---|---|
| Chave privada de assinatura PDF | Gerada em memoria por requisicao; nao armazenada | Nao permite validacao de identidade por cadeia duradoura; descarte depende do GC |
| Segredo JWT | Configuravel por variavel; ha valor padrao/dev versionado | Se reutilizado, permite forjar tokens |
| Senhas PostgreSQL/dev | Valores padrao/dev em `.env`, Compose e YAML | Acesso indevido se usados fora de ambiente local |
| Keystore/certificado | Nao encontrados | Nao ha PKI real nem rotacao/revogacao de certificado |

## Adequacao ao prototipo

RSA 2048, SHA-256, BCrypt e HMAC sao escolhas tecnicas razoaveis para demonstracao academica. As limitacoes principais nao estao no nome dos algoritmos, mas no desenho: ausencia de TLS configurado, ausencia de criptografia em repouso, segredo padrao versionado, chave de assinatura efemera sem certificado confiavel e exposicao de dados pessoais no PDF.
