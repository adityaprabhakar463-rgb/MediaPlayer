# Backend 08 — Sync and Offline

## What syncs

- Favourites, playlists, playlist items, recently played.
- User preferences (reduced effects) via Android DataStore locally; optional server sync later.

## Sync model

- Device registration on first launch.
- Delta cursor (`sync_changes` with `version`) for incremental pulls.
- Conflict resolution: last-write-wins per entity using `version` and `updated_at`; tombstones for deletes.
- Client applies changes and posts `/api/v1/sync/ack`.

## Offline behavior

- Offline cache: metadata only (track/playlist/favourite/recent), no audio.
- Room local database; DataStore for preferences.
- Background sync via WorkManager when network returns.
- No offline audio downloads (Audius terms; session-only caching).

## Limits

- Playlist item cap, favourite cap, recent-play cap (aligned with client caps).
- Sync payload size limits; pagination for large libraries.
