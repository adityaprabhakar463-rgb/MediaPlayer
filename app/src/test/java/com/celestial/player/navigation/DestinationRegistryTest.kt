package com.celestial.player.navigation

import com.celestial.player.ui.navigation.DestinationRegistry
import com.celestial.player.ui.navigation.NavTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DestinationRegistryTest {

    @Test
    fun registryCoversEveryUniverseDestination() {
        val expected = setOf(
            NavTarget.Playlists,
            NavTarget.Search,
            NavTarget.AllSongs,
            NavTarget.Favourites,
            NavTarget.RecentlyPlayed,
        )

        assertEquals(5, DestinationRegistry.entries.size)
        assertEquals(expected, DestinationRegistry.entries.map { it.target }.toSet())
        assertEquals(5, DestinationRegistry.entries.map { it.id }.toSet().size)
    }

    @Test
    fun everyDestinationHasLabelAccessibilityAndOrbit() {
        DestinationRegistry.entries.forEach { entry ->
            assertTrue(entry.label.isNotBlank())
            assertTrue(entry.subtitle.isNotBlank())
            assertTrue(entry.accessibilityText.contains("button"))
            assertTrue(entry.orbitRadius > 0f)
            assertTrue(entry.radius > 0f)
        }
    }
}
