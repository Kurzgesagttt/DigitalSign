---
name: postgres-data-security-analysis
description: 'Analyze PostgreSQL schema, persisted data patterns, privacy risks, and security posture using read-only SELECT queries against the local Docker database. Use for LGPD reviews, PII discovery, schema inspection, constraint analysis, and security-oriented data audits.'
argument-hint: 'Describe the database review you want, for example: inspect PII exposure, weak constraints, and sensitive persisted data'
user-invocable: true
---

# PostgreSQL Data Security Analysis

Use this skill when the task is to inspect the PostgreSQL database structure or persisted data with a focus on data analysis, LGPD, privacy, and security.

## Scope

- Database engine: PostgreSQL
- Runtime source: Docker container `digitalsign-db`
- Intended access: read-only inspection
- Allowed SQL shape: `SELECT` or `WITH` queries only

## Rules

- Never run `INSERT`, `UPDATE`, `DELETE`, `ALTER`, `DROP`, `TRUNCATE`, `GRANT`, `REVOKE`, `CREATE`, or any other write or administrative SQL.
- Prefer metadata inspection first through `information_schema` and `pg_catalog`.
- When checking persisted data, limit results and avoid printing excessive personal data.
- If sensitive data appears, summarize findings instead of echoing raw values unless the user explicitly asks for sample rows.
- Treat database credentials as sensitive configuration and avoid repeating them in chat when not necessary.

## Procedure

1. Confirm the goal of the analysis: schema, privacy exposure, data quality, or security posture.
2. Use [query-postgres-readonly.ps1](./scripts/query-postgres-readonly.ps1) for every database query.
3. Start with metadata queries to map tables, columns, indexes, constraints, and nullable sensitive fields.
4. Inspect persisted data only with bounded queries such as `LIMIT 20`.
5. Report findings focusing on:
   - personal data categories persisted
   - unnecessary exposure of PII or secrets
   - missing uniqueness, foreign keys, or not-null constraints
   - weak auditability or retention concerns
   - values that suggest insecure storage patterns

## Recommended Query Areas

- Tables and ownership
- Columns, types, nullability, defaults
- Primary keys, foreign keys, unique constraints
- Index coverage for identifiers and search columns
- Columns that may store CPF, email, names, tokens, hashes, certificates, URLs, or document metadata
- Row samples limited to small result sets

## Execution Notes

- Prefer running queries through the Docker container instead of requiring a local PostgreSQL client configuration.
- The helper script enforces a read-only transaction and rejects forbidden SQL verbs.
- If the container is not running, report that clearly and stop instead of guessing.