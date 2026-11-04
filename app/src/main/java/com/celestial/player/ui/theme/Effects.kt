package com.celestial.player.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
data class CelestialEffects(
    val reducedMotion: Boolean = false,
    val ambientMotion: Boolean = true,
    val glowIntensity: Float = 1f,
)

val LocalCelestialEffects = staticCompositionLocalOf { CelestialEffects() }
