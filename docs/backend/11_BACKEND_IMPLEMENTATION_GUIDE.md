# Backend 11 — Implementation Guide

## Milestones

### M0 — Skeleton and auth
- Ktor project, PostgreSQL, Flyway, Docker Compose.
- Anonymous device register, JWT access/refresh rotation, device sessions.

### M1 — Catalog and Audius adapter
- `TrackSource` interface + Audius implementation.
- Discovery-node resolution, health/failover, `app_name` param.
- Search + track lookup filtered by curated allowlist.
- Stream URL resolution (`/v1/tracks/{id}/stream`) with per-play resolution.
- Availability filtering and negative-cache TTL.

### M2 — Library and sync
- Playlists CRUD, favourites, recents.
- Delta sync (`sync_changes`), tombstones, client ack.
- Upgrade/link flow merging anonymous data into durable account.

### M3 — Search and hardening
- Postgres FTS/trigram search over curated metadata.
- Rate-limit budget enforcement, 429 handling.
- Contract tests, load tests, health endpoints.
- Exit-strategy adapter ready for Jamendo/licensed/self-hosted.

## Sequencing with frontend

- Backend M0/M1 before Android streaming integration.
- Backend M2 before client sync of favourites/playlists/recents.
- Frontend can develop offline-first (Room/DataStore) against stubbed backend first.

## Acceptance criteria

- Anonymous device can register, play a curated Audius track, favourite it, and sync to a second device after upgrade.
- Unavailable/deleted tracks never reach the player.
- Audius rate budget never exceeded by backend.
- All endpoints have OpenAPI + contract tests.

## Risks

- Audius API changes/termination (adapter boundary mitigates).
- Catalog quality/rights (curated allowlist mitigates).
- Rate-limit exhaustion (server-side budget + client caching mitigates).
