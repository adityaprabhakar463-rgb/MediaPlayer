# Backend 04 — API Contract

## Design principles

- REST v1 under `/api/v1/`.
- OpenAPI is the source of truth; hand-written docs must match it.
- Pagination: cursor-based (`next_cursor`, `limit`).
- Idempotency: `Idempotency-Key` header for state-changing POST/PUT.
- Errors: RFC 9457 `application/problem+json` with `type`, `title`, `status`, `detail`, `instance`.
- All timestamps ISO-8601 UTC.
- Auth: Bearer access token in `Authorization` header; anonymous device auth via `X-Device-Id` / `X-Device-Secret` until upgraded.

## Endpoints

### Auth
- `POST /api/v1/auth/device/register` — register anonymous device; returns device credentials + access/refresh tokens.
- `POST /api/v1/auth/token/refresh` — rotate access + refresh tokens.
- `POST /api/v1/auth/upgrade` — link email+password (or Google later) to anonymous device; merges data.
- `POST /api/v1/auth/revoke` — revoke session/device.

### Catalog
- `GET /api/v1/catalog/search?q=...&limit=&cursor=` — curated catalog search (proxy through adapter + local metadata).
- `GET /api/v1/catalog/tracks/{id}` — track metadata + availability.
- `GET /api/v1/catalog/artists/{id}` — curated artist + tracks.
- `GET /api/v1/catalog/playback/{trackId}` — resolve short-lived stream URL + metadata for play; client calls Audius stream URL directly or via backend proxy.

### Library
- `GET /api/v1/library/playlists` — list playlists.
- `POST /api/v1/library/playlists` — create playlist.
- `GET /api/v1/library/playlists/{id}` — playlist detail + items.
- `PATCH /api/v1/library/playlists/{id}` — rename/reorder.
- `DELETE /api/v1/library/playlists/{id}` — soft delete.
- `GET /api/v1/library/favourites` — list favourited tracks.
- `POST /api/v1/library/favourites/{trackId}` / `DELETE ...` — toggle favourite.
- `GET /api/v1/library/recents` — recently played with recency ordering.
- `POST /api/v1/library/recents` — record play (position, duration).

### Sync
- `GET /api/v1/sync/changes?since_version=` — delta cursor + tombstones for client sync.
- `POST /api/v1/sync/ack` — client acknowledges applied version.

### Health / ops
- `GET /health`, `GET /ready`, `GET /version`.

## Error example

```json
{
  "type": "https://celestial.app/problems/rate-limited",
  "title": "Too many requests",
  "status": 429,
  "detail": "Audius rate budget exhausted",
  "instance": "/api/v1/catalog/search",
  "retry_after": 30
}
```
