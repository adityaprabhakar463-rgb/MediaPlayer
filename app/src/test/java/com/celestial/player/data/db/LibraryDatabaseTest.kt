package com.celestial.player.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryDatabaseTest {

    private lateinit var database: CelestialDatabase
    private lateinit var dao: LibraryDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CelestialDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.libraryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun rescanningKeepsFavouritesPlaylistsAndRecentsForPresentFiles() = runBlocking {
        val track = track("track-a")
        dao.replaceLibrary(listOf(track))
        dao.addFavourite(FavouriteEntity(track.id, 1L))
        dao.insertPlaylist(PlaylistEntity("playlist-1", "Mix", 1L))
        dao.replacePlaylistTracks("playlist-1", listOf(track.id))
        dao.recordRecent(RecentEntity(track.id, 2L))

        dao.replaceLibrary(listOf(track.copy(title = "Renamed after retag")))

        assertEquals(listOf(track.id), dao.observeFavouriteIds().first())
        assertEquals(listOf(track.id), dao.observePlaylistTracks().first().map { it.trackId })
        assertEquals(listOf(track.id), dao.observeRecentTracks().first().map { it.track.id })
        assertEquals("Renamed after retag", dao.observeTracks().first().single().title)
    }

    @Test
    fun rescanningPrunesRowsForFilesThatDisappeared() = runBlocking {
        val oldTrack = track("old")
        dao.replaceLibrary(listOf(oldTrack))
        dao.addFavourite(FavouriteEntity(oldTrack.id, 1L))
        dao.insertPlaylist(PlaylistEntity("playlist-1", "Mix", 1L))
        dao.replacePlaylistTracks("playlist-1", listOf(oldTrack.id))
        dao.recordRecent(RecentEntity(oldTrack.id, 2L))

        val newTrack = track("new")
        dao.replaceLibrary(listOf(newTrack))

        assertTrue(dao.observeFavouriteIds().first().isEmpty())
        assertTrue(dao.observePlaylistTracks().first().isEmpty())
        assertTrue(dao.observeRecentTracks().first().isEmpty())
        assertEquals(listOf(newTrack.id), dao.observeTracks().first().map { it.id })
    }

    @Test
    fun recentsAreTrimmedToNewestHundred() = runBlocking {
        val tracks = (0 until 105).map { track("track-$it") }
        dao.replaceLibrary(tracks)
        tracks.forEachIndexed { index, track ->
            dao.recordRecent(RecentEntity(track.id, index.toLong()))
        }

        dao.trimRecents()

        val recents = dao.observeRecentTracks().first()
        assertEquals(100, recents.size)
        // Oldest five are dropped; newest (track-104) stays at the top.
        assertEquals("track-104", recents.first().track.id)
        assertTrue(recents.none { it.track.id in setOf("track-0", "track-1", "track-2", "track-3", "track-4") })
    }

    private fun track(id: String) = TrackEntity(
        id = id,
        title = id,
        artistId = "artist-$id",
        artistName = "Artist",
        albumId = "album-$id",
        albumTitle = "Album",
        trackIndex = 1,
        durationMs = 60_000L,
        mediaUri = "content://test/$id",
        folderPath = "Album",
        artworkPath = null,
        mimeType = "audio/mpeg",
        dateAddedMs = 1L,
        sizeBytes = 1L,
    )
}
