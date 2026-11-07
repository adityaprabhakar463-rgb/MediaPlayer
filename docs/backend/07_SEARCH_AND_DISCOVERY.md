# Backend 07 — Search and Discovery

## Scope

Search is over the curated catalog only, not the full Audius global catalog. This keeps results on-brand, licensed-clear, and performant.

## Backend search

- Postgres full-text search (`to_tsquery`/`plainto_tsquery`) and/or `pg_trgm` similarity over title, artist, album.
- Ranking: exact match > curated popularity > recency.
- Category results: tracks, artists, playlists.
- Suggestions: recent queries (device-scoped), curated featured artists.
- Empty states when no curated match.

## Frontend search (via backend)

- Debounce 280 ms on query.
- Clear search, recent searches, no-results state.
- Tapping a song resolves playback through `/api/v1/catalog/playback/{trackId}`.
- Category tabs map to backend search filters.

## Deferred

- Recommendations, "more like this", global Audius search, trending charts.
