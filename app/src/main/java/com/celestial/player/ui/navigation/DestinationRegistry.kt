package com.celestial.player.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Single source of truth for the seven universe destinations.
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
            orbitRadius = 3.6f,
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
            orbitRadius = 5.2f,
            orbitAngle = 1.8f,
            orbitSpeed = 0.03f,
            inclinationDeg = -6f,
            radius = 0.58f,
        ),
        DestinationEntry(
            id = "albums",
            target = NavTarget.Albums,
            label = "Albums",
            subtitle = "Compact Sound Spheres",
            icon = Icons.Filled.Album,
            accessibilityText = "Albums destination, button",
            primaryColor = Color(0xFFEC4899),
            secondaryColor = Color(0xFFF472B6),
            atmosphereColor = Color(0xFFFBCFE8),
            orbitRadius = 6.8f,
            orbitAngle = 3.4f,
            orbitSpeed = 0.024f,
            inclinationDeg = 8f,
            radius = 0.52f,
            hasRings = true,
            ringInnerRadius = 0.68f,
            ringOuterRadius = 0.92f,
            ringColor = Color(0x66F472B6),
        ),
        DestinationEntry(
            id = "artists",
            target = NavTarget.Artists,
            label = "Artists",
            subtitle = "Creators & Solar Systems",
            icon = Icons.Filled.Person,
            accessibilityText = "Artists destination, button",
            primaryColor = Color(0xFF10B981),
            secondaryColor = Color(0xFF34D399),
            atmosphereColor = Color(0xFFA7F3D0),
            orbitRadius = 8.4f,
            orbitAngle = 4.8f,
            orbitSpeed = 0.018f,
            inclinationDeg = -4f,
            radius = 0.56f,
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
            orbitRadius = 10.0f,
            orbitAngle = 0.9f,
            orbitSpeed = 0.014f,
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
            orbitRadius = 11.6f,
            orbitAngle = 2.6f,
            orbitSpeed = 0.010f,
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
            orbitRadius = 13.2f,
            orbitAngle = 5.6f,
            orbitSpeed = 0.008f,
            inclinationDeg = 5f,
            radius = 0.46f,
        ),
    )

    fun find(target: NavTarget): DestinationEntry? = entries.firstOrNull { it.target == target }
}
