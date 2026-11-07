# Backend 02 — Architecture and Stack

## Architecture

Ktor modular monolith. Domain layer is pure Kotlin with no Android imports. Modules point inward:

- `auth` — anonymous device bootstrap, JWT access/refresh rotation, upgrade/link flow.
- `catalog` — curated artist/track metadata, search, availability.
- `library` — playlists, favourites, recents, sync delta.
- `audius-adapter` — Audius discovery-node resolution, search proxy, stream-URL resolution, rate-limit budget.
- `search` — Postgres full-text search over curated metadata.
- `admin` — curated artist allowlist, takedown suppression, audit log.

Provider boundary: `TrackSource` interface with implementations per provider (Audius first). Swapping providers changes only the adapter and catalog ingestion path.

## Stack

- Language: Kotlin 2.4.10 (K2).
- Framework: Ktor (REST, content negotiation, kotlinx.serialization).
- Database: PostgreSQL; Flyway migrations; Exposed or plain SQL (decide in implementation).
- DI: Koin (backend) or manual; keep consistent with frontend where shared logic exists.
- Auth: JWT access + refresh tokens, refresh rotation, Argon2id for upgraded passwords.
- Deployment: Docker Compose dev (Postgres + backend), CI/CD build and push.
- Client contract: OpenAPI source of truth; Android consumes via Ktor client or Retrofit + kotlinx.serialization.

## Versions (align with app where sensible)

- Kotlin 2.4.10, AGP 9.5 on Android side.
- Compose BOM 2026.09 on Android side.
- Backend dependencies pinned in Gradle version catalog or Gradle.lockfile.

## Boundaries

- Backend never stores audio bytes in the prototype.
- Backend never persists Audius stream URLs long-term; they are short-lived signed URLs resolved per session.
- Client stores only metadata + external IDs; audio URLs resolved at play time.
