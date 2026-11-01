# Diario de evidencias

| Data/hora | Commit/branch | Ambiente | Comando/teste | Resultado resumido | Arquivo de evidencia | Relacao com requisito/risco |
|---|---|---|---|---|---|---|
| 2026-08-07 | `main` / `b1e786bb6dc3a62c57b618f738ef5a81609d1c86` | PowerShell, Windows | `git status --short` | Working tree com alteracoes modificadas e arquivos nao rastreados | Saida do comando na sessao Codex | Escopo/estado analisado |
| 2026-08-07 00:14 | mesmo | `DigitalSignature` | `mvn test` | Sucesso: 5 testes, 0 falhas; warning PDFBox sobre cache em home sem permissao | Saida do Maven | Evidencia de testes backend; risco temporarios/cache |
| 2026-08-07 00:14 | mesmo | `DigitalSignatureFront` sandbox | `npm test` | Falhou com `spawn EPERM` ao iniciar esbuild | Saida npm | Limitacao ambiental |
| 2026-08-07 00:14 | mesmo | `DigitalSignatureFront` com permissao elevada | `npm test` | Sucesso: 2 arquivos, 3 testes | Saida Vitest | Evidencia frontend |
| 2026-08-07 00:14 | mesmo | `DigitalSignatureFront` | `npm run lint` | Sucesso sem erros | Saida ESLint | Qualidade frontend |
| 2026-08-07 00:14 | mesmo | `DigitalSignatureFront` sandbox | `npm run build` | Falhou com `spawn EPERM` ao iniciar esbuild | Saida npm | Limitacao ambiental |
| 2026-08-07 00:14 | mesmo | `DigitalSignatureFront` com permissao elevada | `npm run build` | Sucesso; bundle Vite gerado | Saida Vite | Evidencia build |
| 2026-08-07 00:14 | mesmo | `DigitalSignatureFront` | `npm audit --audit-level=high` | 0 vulnerabilidades reportadas | Saida npm audit | Dependencias frontend |
| 2026-08-07 00:15 | mesmo | `DigitalSignature` com permissao elevada | `mvn dependency:tree -Dscope=compile` | Sucesso; inventario de dependencias Maven | Saida Maven | Inventario backend |
| 2026-08-07 | mesmo | Inspecao estatica | Leitura de controllers, services, models, repositories, configs, frontend | Endpoints, dados, controles e lacunas mapeados | `docs/pic/*.md` | Todos os requisitos do diagnostico |
