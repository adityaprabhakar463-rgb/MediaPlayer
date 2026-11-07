package com.celestial.player.scene.model

import androidx.compose.ui.graphics.Color
import com.celestial.player.scene.math.Vector3
import com.celestial.player.ui.navigation.DestinationRegistry
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
        fun defaultPlanets(): List<Planet3D> = DestinationRegistry.entries.map { entry ->
            Planet3D(
                id = entry.id,
                target = entry.target,
                name = entry.label,
                subtitle = entry.subtitle,
                orbitRadius = entry.orbitRadius,
                orbitAngle = entry.orbitAngle,
                orbitSpeed = entry.orbitSpeed,
                inclinationDeg = entry.inclinationDeg,
                radius = entry.radius,
                primaryColor = entry.primaryColor,
                secondaryColor = entry.secondaryColor,
                atmosphereColor = entry.atmosphereColor,
                hasRings = entry.hasRings,
                ringInnerRadius = entry.ringInnerRadius,
                ringOuterRadius = entry.ringOuterRadius,
                ringColor = entry.ringColor,
            )
        }

        fun findPlanetForTarget(target: NavTarget): Planet3D? =
            defaultPlanets().firstOrNull { it.target == target }
    }
}
