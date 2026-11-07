CREATE TABLE users (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    device_id VARCHAR(255) UNIQUE NOT NULL,
    anonymous BOOLEAN NOT NULL DEFAULT TRUE,
    linked_email VARCHAR(255) UNIQUE,
    linked_password_hash VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_seen_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE devices (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    installation_id VARCHAR(255) UNIQUE NOT NULL,
    device_secret VARCHAR(255) NOT NULL,
    device_info TEXT,
    registered_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    revoked_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE sessions (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_id UUID REFERENCES devices(id) ON DELETE SET NULL,
    refresh_token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    revoked_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE curated_artists (
    id VARCHAR(255) PRIMARY KEY,
    audius_user_id VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    handle VARCHAR(255) NOT NULL,
    is_verified BOOLEAN NOT NULL DEFAULT TRUE,
    approved_by VARCHAR(255),
    approved_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    removed_at TIMESTAMP WITH TIME ZONE,
    removal_reason TEXT
);

CREATE TABLE tracks (
    id VARCHAR(255) PRIMARY KEY,
    external_id VARCHAR(255) UNIQUE NOT NULL,
    provider VARCHAR(50) NOT NULL DEFAULT 'audius',
    title VARCHAR(500) NOT NULL,
    artist_name VARCHAR(255) NOT NULL,
    album_name VARCHAR(255),
    duration_ms INT NOT NULL,
    artwork_url TEXT,
    is_streamable BOOLEAN NOT NULL DEFAULT TRUE,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    is_delete BOOLEAN NOT NULL DEFAULT FALSE,
    curated_artist_id VARCHAR(255) REFERENCES curated_artists(id) ON DELETE SET NULL,
    version BIGINT NOT NULL DEFAULT 1,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE playlists (
    id VARCHAR(255) PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    version BIGINT NOT NULL DEFAULT 1,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE playlist_items (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    playlist_id VARCHAR(255) NOT NULL REFERENCES playlists(id) ON DELETE CASCADE,
    track_id VARCHAR(255) NOT NULL,
    position INT NOT NULL,
    added_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE favourites (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    track_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, track_id)
);

CREATE TABLE recent_plays (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    track_id VARCHAR(255) NOT NULL,
    played_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    position_ms INT DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 1
);

CREATE TABLE sync_changes (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(255) NOT NULL,
    operation VARCHAR(20) NOT NULL,
    version BIGINT NOT NULL,
    device_id UUID REFERENCES devices(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tracks_curated ON tracks(curated_artist_id);
CREATE INDEX idx_sync_changes_user_version ON sync_changes(user_id, version);
