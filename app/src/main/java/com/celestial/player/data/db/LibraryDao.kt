package com.celestial.player.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {

    @Query("SELECT * FROM tracks ORDER BY title COLLATE NOCASE ASC")
    fun observeTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks")
    suspend fun getTracks(): List<TrackEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Query("DELETE FROM tracks")
    suspend fun deleteAllTracks()

    @Query("DELETE FROM favourites WHERE trackId NOT IN (SELECT id FROM tracks)")
    suspend fun pruneFavourites()

    @Query("DELETE FROM recents WHERE trackId NOT IN (SELECT id FROM tracks)")
    suspend fun pruneRecents()

    @Query("DELETE FROM playlist_tracks WHERE trackId NOT IN (SELECT id FROM tracks)")
    suspend fun prunePlaylistTracks()

    /**
     * Replaces the indexed file set atomically. Favourites, playlist entries and recents that point
     * at files which disappeared are removed by the explicit prune queries (there are deliberately
     * no foreign keys, so user data never cascades away during a rescan).
     */
    @Transaction
    suspend fun replaceLibrary(tracks: List<TrackEntity>) {
        deleteAllTracks()
        if (tracks.isNotEmpty()) insertTracks(tracks)
        pruneFavourites()
        pruneRecents()
        prunePlaylistTracks()
    }

    @Query("SELECT * FROM playlists ORDER BY name COLLATE NOCASE ASC")
    fun observePlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlist_tracks ORDER BY position ASC")
    fun observePlaylistTracks(): Flow<List<PlaylistTrackEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylist(id: String): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: String)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun clearPlaylistTracks(playlistId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTracks(entries: List<PlaylistTrackEntity>)

    @Transaction
    suspend fun replacePlaylistTracks(playlistId: String, trackIds: List<String>) {
        clearPlaylistTracks(playlistId)
        insertPlaylistTracks(
            trackIds.mapIndexed { index, trackId ->
                PlaylistTrackEntity(playlistId = playlistId, trackId = trackId, position = index)
            },
        )
    }

    @Query("SELECT trackId FROM favourites ORDER BY addedAtMs DESC")
    fun observeFavouriteIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavourite(favourite: FavouriteEntity)

    @Query("DELETE FROM favourites WHERE trackId = :trackId")
    suspend fun removeFavourite(trackId: String)

    @Query(
        """
        SELECT t.*, r.playedAtMs AS playedAtMs
        FROM recents r
        INNER JOIN tracks t ON t.id = r.trackId
        ORDER BY r.playedAtMs DESC
        LIMIT :limit
        """,
    )
    fun observeRecentTracks(limit: Int = 100): Flow<List<RecentTrackRow>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordRecent(recent: RecentEntity)

    @Query(
        """
        DELETE FROM recents WHERE trackId NOT IN (
            SELECT trackId FROM recents ORDER BY playedAtMs DESC LIMIT 100
        )
        """,
    )
    suspend fun trimRecents()
}
