package com.celestial.player.navigation

import com.celestial.player.ui.navigation.CelestialNavState
import com.celestial.player.ui.navigation.NavTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CelestialNavStateTest {

    @Test
    fun testNavigationAndBackstack() {
        val nav = CelestialNavState()

        assertEquals(NavTarget.Universe, nav.current)
        assertTrue(nav.isAtRoot())
        assertNull(nav.previousDestination)

        nav.navigate(NavTarget.Playlists)
        assertEquals(NavTarget.Playlists, nav.current)
        assertFalse(nav.isAtRoot())
        assertNull(nav.previousDestination)

        nav.back()
        assertEquals(NavTarget.Universe, nav.current)
        assertTrue(nav.isAtRoot())
        assertEquals(NavTarget.Playlists, nav.previousDestination)
    }

    @Test
    fun testNestedNavigationAndBack() {
        val nav = CelestialNavState()

        nav.navigate(NavTarget.Favourites)
        nav.navigate(NavTarget.AllSongs)
        assertEquals(NavTarget.AllSongs, nav.current)

        nav.back()
        assertEquals(NavTarget.Favourites, nav.current)
        assertNull(nav.previousDestination)

        nav.back()
        assertEquals(NavTarget.Universe, nav.current)
        assertEquals(NavTarget.Favourites, nav.previousDestination)
    }
}
