# Backend 03 — Domain and Data Model

## Core entities

### User
- id (UUID/ULID), device_id, anonymous flag, linked_email, linked_provider, created_at, last_seen_at, deleted_at (soft delete).
- Anonymous users bootstrap without credentials; upgrade/link merges history into a durable account.

### Device
- id, user_id, installation_id, device_secret, device_info, registered_at, revoked_at.

### Session / Token
- access_token (JWT), refresh_token (hashed), expires_at, device_id, created_at, revoked_at.
- Refresh rotation: new refresh token issued per use, old revoked.

### Track
- id (UUID), external_id (Audius track id), provider (`audius`), title, artist_name, album_name, duration_ms, artwork_url, is_streamable, is_available, is_delete, curated_artist_id, version, updated_at.
- Stream URL is NOT stored; resolved from Audius per play request.

### Playlist
- id, user_id, name, version, deleted_at, created_at, updated_at.

### PlaylistItem
- id, playlist_id, track_id, position, added_at.

### Favourite
- id, user_id, track_id, created_at.

### RecentPlay
- id, user_id, track_id, played_at, position_ms (optional), version.

### SyncChange
- id, entity_type, entity_id, operation (create/update/delete), version, device_id, created_at, applied_at.

### CuratedArtist
- id, audius_user_id, name, handle, is_verified, approved_by, approved_at, removed_at, removal_reason.

## Constraints

- IDs: UUID or ULID; never expose internal sequential IDs externally.
- Timestamps: UTC, `created_at`/`updated_at` on every mutable table.
- Soft deletes with `deleted_at`/`removed_at`; tombstones emitted via `sync_changes`.
- Optimistic concurrency via `version` column.
- Foreign keys with cascade only where safe; playlist items cascade on playlist delete.

## Relationships

- User 1:N Device, Session, Playlist, Favourite, RecentPlay, SyncChange.
- CuratedArtist 1:N Track.
- Playlist 1:N PlaylistItem.
- Track 1:N PlaylistItem, Favourite, RecentPlay.
