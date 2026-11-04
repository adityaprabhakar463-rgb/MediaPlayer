package com.celestial.player.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class CelestialColors(
    val spaceVoid: Color,
    val deepSpace: Color,
    val deepSurface: Color,
    val nebula: Color,
    val cosmicViolet: Color,
    val starlight: Color,
    val moonlight: Color,
    val auroraBlue: Color,
    val glassBorder: Color,
)

val DefaultCelestialColors = CelestialColors(
    spaceVoid = SpaceVoid,
    deepSpace = DeepSpace,
    deepSurface = DeepSurface,
    nebula = Nebula,
    cosmicViolet = CosmicViolet,
    starlight = Starlight,
    moonlight = Moonlight,
    auroraBlue = AuroraBlue,
    glassBorder = GlassBorder,
)

val LocalCelestialColors = staticCompositionLocalOf { DefaultCelestialColors }
