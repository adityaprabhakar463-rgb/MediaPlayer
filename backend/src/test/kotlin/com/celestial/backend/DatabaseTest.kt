package com.celestial.backend

import com.celestial.backend.database.DatabaseFactory
import com.celestial.backend.database.PostgresLibraryStore
import com.celestial.backend.library.InvalidDeviceSecret
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DatabaseTest {
    @Test
    fun testPostgresLibraryStorePersistence() {
        DatabaseFactory.init("jdbc:postgresql://localhost:5432/celestial", "celestial", "celestial_secret")
        val store = PostgresLibraryStore()

        val (first, created1) = store.registerDevice("db-test-inst-1", "secret-12345", "test device")
        assertTrue(created1)

        val (second, created2) = store.registerDevice("db-test-inst-1", "secret-12345", "test device")
        assertFalse(created2)
        assertEquals(first.id, second.id)

        var caughtMismatch = false
        try {
            store.registerDevice("db-test-inst-1", "different-secret-999", null)
        } catch (_: InvalidDeviceSecret) {
            caughtMismatch = true
        }
        assertTrue(caughtMismatch)

        val playlists = store.playlists(first.id)
        assertEquals(5, playlists.size)

        val createdPlaylist = store.createPlaylist(first.id, "DB Playlist", listOf(playlists.first().trackIds.first()))
        assertEquals("DB Playlist", createdPlaylist.name)

        val fetchedPlaylist = store.playlist(first.id, createdPlaylist.id)
        assertEquals(createdPlaylist.id, fetchedPlaylist.id)

        val favouritesBefore = store.favourites(first.id)
        assertTrue(favouritesBefore.isEmpty())

        val favTrackId = playlists.first().trackIds.first()
        store.addFavourite(first.id, favTrackId)
        val favouritesAfter = store.favourites(first.id)
        assertEquals(1, favouritesAfter.size)
        assertEquals(favTrackId, favouritesAfter.first().trackId)

        store.removeFavourite(first.id, favTrackId)
        assertTrue(store.favourites(first.id).isEmpty())
    }
}
