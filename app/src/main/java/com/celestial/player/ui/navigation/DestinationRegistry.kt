package com.celestial.player.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Single source of truth for the five universe destinations.
 *
 * The 3D scene, planet labels, the accessible Destinations sheet and screen-reader actions all read
 * from this registry so a destination can never appear in one place but be missing in another.
 */
data class DestinationEntry(
    val id: String,
    val target: NavTarget,
    val label: String,
    val subtitle: String,
    val icon: ImageVector,
    val accessibilityText: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val atmosphereColor: Color,
    val orbitRadius: Float,
    val orbitAngle: Float,
    val orbitSpeed: Float,
    val inclinationDeg: Float,
    val radius: Float,
    val hasRings: Boolean = false,
    val ringInnerRadius: Float = 0f,
    val ringOuterRadius: Float = 0f,
    val ringColor: Color = Color.Transparent,
)

object DestinationRegistry {

    val entries: List<DestinationEntry> = listOf(
        DestinationEntry(
            id = "playlists",
            target = NavTarget.Playlists,
            label = "Playlists",
            subtitle = "Curated Constellations",
            icon = Icons.AutoMirrored.Filled.QueueMusic,
            accessibilityText = "Playlists destination, button",
            primaryColor = Color(0xFF8B5CF6),
            secondaryColor = Color(0xFFC084FC),
            atmosphereColor = Color(0xFFA78BFA),
            orbitRadius = 4.0f,
            orbitAngle = 0.4f,
            orbitSpeed = 0.04f,
            inclinationDeg = 4f,
            radius = 0.48f,
            hasRings = true,
            ringInnerRadius = 0.62f,
            ringOuterRadius = 0.95f,
            ringColor = Color(0x66C084FC),
        ),
        DestinationEntry(
            id = "all_songs",
            target = NavTarget.AllSongs,
            label = "All Songs",
            subtitle = "Complete Stellar Library",
            icon = Icons.Filled.LibraryMusic,
            accessibilityText = "All Songs destination, button",
            primaryColor = Color(0xFF3B82F6),
            secondaryColor = Color(0xFF60A5FA),
            atmosphereColor = Color(0xFF93C5FD),
            orbitRadius = 6.2f,
            orbitAngle = 1.7f,
            orbitSpeed = 0.03f,
            inclinationDeg = -6f,
            radius = 0.58f,
        ),
        DestinationEntry(
            id = "search",
            target = NavTarget.Search,
            label = "Search",
            subtitle = "Cosmic Radar",
            icon = Icons.Filled.Search,
            accessibilityText = "Search destination, button",
            primaryColor = Color(0xFF06B6D4),
            secondaryColor = Color(0xFF22D3EE),
            atmosphereColor = Color(0xFFA5F3FC),
            orbitRadius = 8.4f,
            orbitAngle = 3.0f,
            orbitSpeed = 0.022f,
            inclinationDeg = 12f,
            radius = 0.44f,
        ),
        DestinationEntry(
            id = "favourites",
            target = NavTarget.Favourites,
            label = "Favourites",
            subtitle = "Beloved Stars",
            icon = Icons.Filled.Favorite,
            accessibilityText = "Favourites destination, button",
            primaryColor = Color(0xFFF59E0B),
            secondaryColor = Color(0xFFFBBF24),
            atmosphereColor = Color(0xFFFDE68A),
            orbitRadius = 10.6f,
            orbitAngle = 4.3f,
            orbitSpeed = 0.015f,
            inclinationDeg = -9f,
            radius = 0.50f,
        ),
        DestinationEntry(
            id = "recent",
            target = NavTarget.RecentlyPlayed,
            label = "Recently Played",
            subtitle = "Echoes in Orbit",
            icon = Icons.Filled.History,
            accessibilityText = "Recently Played destination, button",
            primaryColor = Color(0xFF6366F1),
            secondaryColor = Color(0xFF818CF8),
            atmosphereColor = Color(0xFFC7D2FE),
            orbitRadius = 12.8f,
            orbitAngle = 5.5f,
            orbitSpeed = 0.010f,
            inclinationDeg = 5f,
            radius = 0.46f,
        ),
    )

    fun find(target: NavTarget): DestinationEntry? = entries.firstOrNull { it.target == target }
}
