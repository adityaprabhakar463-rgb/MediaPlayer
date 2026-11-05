package com.celestial.player.scene.model

import androidx.compose.ui.graphics.Color
import com.celestial.player.scene.math.Vector3
import com.celestial.player.ui.navigation.NavTarget
import kotlin.math.cos
import kotlin.math.sin

data class Planet3D(
    val id: String,
    val target: NavTarget,
    val name: String,
    val subtitle: String,
    val orbitRadius: Float,
    var orbitAngle: Float,
    val orbitSpeed: Float,
    val inclinationDeg: Float,
    val radius: Float,
    val primaryColor: Color,
    val secondaryColor: Color,
    val atmosphereColor: Color,
    val hasRings: Boolean = false,
    val ringInnerRadius: Float = 0f,
    val ringOuterRadius: Float = 0f,
    val ringColor: Color = Color.Transparent,
) {
    fun calculatePosition(timeSec: Float): Vector3 {
        val currentAngle = orbitAngle + timeSec * orbitSpeed
        val incRad = Math.toRadians(inclinationDeg.toDouble()).toFloat()
        val rawX = orbitRadius * cos(currentAngle)
        val rawZ = orbitRadius * sin(currentAngle)

        val x = rawX
        val y = rawZ * sin(incRad)
        val z = rawZ * cos(incRad)

        return Vector3(x, y, z)
    }

    companion object {
        fun defaultPlanets(): List<Planet3D> = listOf(
            Planet3D(
                id = "playlists",
                target = NavTarget.Playlists,
                name = "Playlists",
                subtitle = "Curated Constellations",
                orbitRadius = 3.6f,
                orbitAngle = 0.4f,
                orbitSpeed = 0.04f,
                inclinationDeg = 4f,
                radius = 0.48f,
                primaryColor = Color(0xFF8B5CF6), // Violet
                secondaryColor = Color(0xFFC084FC),
                atmosphereColor = Color(0xFFA78BFA),
                hasRings = true,
                ringInnerRadius = 0.62f,
                ringOuterRadius = 0.95f,
                ringColor = Color(0x66C084FC),
            ),
            Planet3D(
                id = "all_songs",
                target = NavTarget.AllSongs,
                name = "All Songs",
                subtitle = "Complete Stellar Library",
                orbitRadius = 5.2f,
                orbitAngle = 1.8f,
                orbitSpeed = 0.03f,
                inclinationDeg = -6f,
                radius = 0.58f,
                primaryColor = Color(0xFF3B82F6), // Blue
                secondaryColor = Color(0xFF60A5FA),
                atmosphereColor = Color(0xFF93C5FD),
            ),
            Planet3D(
                id = "albums",
                target = NavTarget.Albums,
                name = "Albums",
                subtitle = "Compact Sound Spheres",
                orbitRadius = 6.8f,
                orbitAngle = 3.4f,
                orbitSpeed = 0.024f,
                inclinationDeg = 8f,
                radius = 0.52f,
                primaryColor = Color(0xFFEC4899), // Rose
                secondaryColor = Color(0xFFF472B6),
                atmosphereColor = Color(0xFFFBCFE8),
                hasRings = true,
                ringInnerRadius = 0.68f,
                ringOuterRadius = 0.92f,
                ringColor = Color(0x66F472B6),
            ),
            Planet3D(
                id = "artists",
                target = NavTarget.Artists,
                name = "Artists",
                subtitle = "Creators & Solar Systems",
                orbitRadius = 8.4f,
                orbitAngle = 4.8f,
                orbitSpeed = 0.018f,
                inclinationDeg = -4f,
                radius = 0.56f,
                primaryColor = Color(0xFF10B981), // Emerald
                secondaryColor = Color(0xFF34D399),
                atmosphereColor = Color(0xFFA7F3D0),
            ),
            Planet3D(
                id = "search",
                target = NavTarget.Search,
                name = "Search",
                subtitle = "Cosmic Radar",
                orbitRadius = 10.0f,
                orbitAngle = 0.9f,
                orbitSpeed = 0.014f,
                inclinationDeg = 12f,
                radius = 0.44f,
                primaryColor = Color(0xFF06B6D4), // Cyan
                secondaryColor = Color(0xFF22D3EE),
                atmosphereColor = Color(0xFFA5F3FC),
            ),
            Planet3D(
                id = "favourites",
                target = NavTarget.Favourites,
                name = "Favourites",
                subtitle = "Beloved Stars",
                orbitRadius = 11.6f,
                orbitAngle = 2.6f,
                orbitSpeed = 0.010f,
                inclinationDeg = -9f,
                radius = 0.50f,
                primaryColor = Color(0xFFF59E0B), // Amber
                secondaryColor = Color(0xFFFBBF24),
                atmosphereColor = Color(0xFFFDE68A),
            ),
            Planet3D(
                id = "recent",
                target = NavTarget.RecentlyPlayed,
                name = "Recently Played",
                subtitle = "Echoes in Orbit",
                orbitRadius = 13.2f,
                orbitAngle = 5.6f,
                orbitSpeed = 0.008f,
                inclinationDeg = 5f,
                radius = 0.46f,
                primaryColor = Color(0xFF6366F1), // Indigo
                secondaryColor = Color(0xFF818CF8),
                atmosphereColor = Color(0xFFC7D2FE),
            ),
        )

        fun findPlanetForTarget(target: NavTarget): Planet3D? =
            defaultPlanets().firstOrNull { it.target == target }
    }
}
