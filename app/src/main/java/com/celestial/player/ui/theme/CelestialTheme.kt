package com.celestial.player.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

private val CelestialColorScheme = darkColorScheme(
    primary = CosmicViolet,
    onPrimary = OnPrimaryDark,
    primaryContainer = Nebula,
    onPrimaryContainer = Starlight,
    secondary = AuroraBlue,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SurfaceContainerHigh,
    onSecondaryContainer = Starlight,
    tertiary = Starlight,
    onTertiary = OnTertiaryDark,
    background = SpaceVoid,
    onBackground = Starlight,
    surface = DeepSpace,
    onSurface = Starlight,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = DeepSurface,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = Nebula,
    onSurfaceVariant = Moonlight,
    outline = Color.White.copy(alpha = 0.20f),
    outlineVariant = Color.White.copy(alpha = 0.10f),
    error = CelestialError,
    onError = OnErrorDark,
    errorContainer = CelestialErrorContainer,
    onErrorContainer = Starlight,
    inverseSurface = Nebula,
    inverseOnSurface = Starlight,
    inversePrimary = CosmicViolet,
    scrim = Color.Black,
)

object CelestialTheme {
    val colors: CelestialColors
        @Composable @ReadOnlyComposable get() = LocalCelestialColors.current

    val spacing: CelestialSpacing
        @Composable @ReadOnlyComposable get() = LocalCelestialSpacing.current

    val effects: CelestialEffects
        @Composable @ReadOnlyComposable get() = LocalCelestialEffects.current
}

@Composable
fun CelestialTheme(
    effects: CelestialEffects = CelestialEffects(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalCelestialColors provides DefaultCelestialColors,
        LocalCelestialSpacing provides CelestialSpacing(),
        LocalCelestialEffects provides effects,
    ) {
        MaterialTheme(
            colorScheme = CelestialColorScheme,
            typography = CelestialTypography,
            shapes = CelestialShapes,
            content = content,
        )
    }
}
