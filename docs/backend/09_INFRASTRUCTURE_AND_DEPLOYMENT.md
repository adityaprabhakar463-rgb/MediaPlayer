# Backend 09 — Infrastructure and Deployment

## Local development

- Docker Compose: PostgreSQL + backend (Ktor) + optional MinIO if audio storage is added later.
- Environment variables for database URL, JWT secret, Audius budget config, log level.
- Flyway migrations run on startup or CI.

## Environments

- Dev: Docker Compose.
- Staging/Production: container image; PostgreSQL managed or self-hosted; object storage only if audio storage is added.
- No CDN in prototype (no audio hosting).

## CI/CD

- Build, test, lint, containerize on every push.
- Staging deploy on merge; production deploy manual/staged rollout.
- Baseline profiles and APK/AAB handled on Android side separately.

## Observability

- Structured JSON logs, metrics, health endpoints (`/health`, `/ready`).
- Tracing optional; add when latency debugging is needed.

## Backups and security

- PostgreSQL regular backups; point-in-time recovery for production.
- Database credentials in secrets manager/env, never in repo.
- JWT signing key rotated periodically.
