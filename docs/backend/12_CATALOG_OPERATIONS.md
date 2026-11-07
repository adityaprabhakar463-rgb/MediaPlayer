# Backend 12 — Catalog Operations

## Curated artist allowlist

- Only approved Audius artists are searchable/playable.
- Approval: verified Audius flag + manual backend approval.
- Fields: audius_user_id, name, handle, is_verified, approved_by, approved_at, removed_at, removal_reason.

## Operations

- Add artist: backend admin inserts curated artist; backend backfills recent tracks via adapter.
- Remove artist: soft delete; emit tombstones via `sync_changes`; clients hide the artist/tracks.
- Takedown suppression: remove tracks/artists on rights holder request; audit log.
- Broken-track suppression: tracks with `is_delete`/`is_streamable=false` hidden until re-available.

## Licensing

- Audius content is under artist-granted Open Music License; app must surface attribution per provider terms.
- Backend logs which curated tracks are served for audit.
- No claim of ownership; no ML/AI training on Audius content per API terms.

## Audit

- Log curated add/remove, takedowns, and sync of track availability.
- Retain logs per retention policy; no PII/tokens.
