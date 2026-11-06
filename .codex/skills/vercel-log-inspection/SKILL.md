---
name: vercel-log-inspection
description: Use when a user asks to inspect or diagnose Vercel build/runtime logs, failed or stale deployments, or frontend-to-API connection errors in a Vercel-hosted app.
---

# Vercel Log Inspection

Use this skill for read-only diagnosis of Vercel deployments. Report what the logs prove, separate that from hypotheses, and do not claim the application is healthy just because its build succeeded.

## Workflow

1. Identify the linked project and current deployment. From the project directory, check the Vercel link without printing project identifiers, then use `vercel ls --yes`. Distinguish Production from Preview and note deployment status, age, URL, branch, and commit when available.
2. Inspect the specific deployment rather than guessing from the newest row. Use `vercel inspect <deployment-url>` for deployment metadata and `vercel inspect <deployment-url> --logs` for build logs. Compare the deployed branch/commit with the repository branch and latest commit to identify stale or failed deployments.
3. For runtime output, use `vercel logs <deployment-url> --json`. This streams runtime logs for a Ready deployment for up to five minutes; it is not a historical build-log command. Stop once the relevant time window is covered and summarize only relevant, redacted lines.
4. If configuration is relevant, use `vercel env ls <environment>` to check variable names and which environment has them. Never run `vercel env pull`, print variable values, or copy secrets into the report. For Vite, `VITE_*` values are embedded at build time, so a changed value requires a new deployment.
5. Correlate the evidence. A successful frontend build proves only that the frontend compiled. Vercel frontend logs do not show requests made by a user's browser to an external backend. If the browser calls localhost, it targets the visitor's own device. If the backend is separately hosted, inspect its logs only through its own authorized provider/tool.
6. Report each finding with deployment URL/environment, branch and commit if available, timestamp, exact sanitized log evidence, likely cause, confidence, and the next safe action. State explicitly when logs cannot establish the backend's health.

## Authentication and Safety

- If the CLI reports an invalid or missing token, do not print, request, or reuse a token. If the user has authorized Vercel access, start `vercel login` and let the user complete its device flow; share the short-lived device code only with that user.
- Treat runtime/build output as potentially containing personal data, tokens, URLs with credentials, or request payloads. Inspect the narrowest relevant deployment and redact before quoting.
- This skill is read-only. Do not deploy, promote, change environment variables, relink projects, or modify settings as part of log inspection. Handle those only when separately requested and authorized.
- Do not curl or fetch the deployed URL as a way to verify it. Use Vercel deployment metadata and logs; use a browser workflow only when the user asks for browser-level behavior.

## Common Misreads

- `vercel inspect ... --logs` shows build logs; `vercel logs ...` shows live runtime logs.
- A Ready deployment may still point to a dead or misconfigured API.
- CORS errors, connection refused, and DNS/TLS failures have different causes. Use browser evidence and backend/provider logs before attributing an API failure to Vercel.
- A missing production variable and a wrong production variable are different findings. Do not infer a variable's value from its name.
