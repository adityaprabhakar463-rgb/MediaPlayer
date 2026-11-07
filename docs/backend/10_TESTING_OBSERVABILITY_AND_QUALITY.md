# Backend 10 — Testing, Observability, and Quality

## Testing strategy

- Unit tests for domain logic (auth merge, sync conflict resolution, availability filtering).
- Testcontainers for PostgreSQL integration tests.
- Contract tests against OpenAPI spec.
- Load tests for search and playback resolution (Audius budget protection).
- Security tests for auth endpoints and token rotation.

## Observability

- Structured logs (no PII/tokens).
- Metrics: request rate, latency, error rate, Audius budget consumption.
- Traces optional.
- Health endpoints: `/health`, `/ready`.

## Quality gates

- CI: build → test → lint → container build.
- Detekt/ktlint clean on backend Kotlin.
- OpenAPI spec validated before contract changes.
- No secrets committed; secret scanning in CI.

## Seed data

- Curated artist seed list for dev/staging; clearly marked as sample.
