package com.celestial.player.ui.components.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.ui.graphics.vector.ImageVector
import com.celestial.player.ui.theme.CelestialTheme
import com.celestial.player.ui.theme.CelestialColors

fun artworkColors(colorSeed: Int, colors: CelestialColors): List<Color> {
    val accent = when (colorSeed % 4) {
        0 -> colors.cosmicViolet
        1 -> colors.auroraBlue
        2 -> lerp(colors.cosmicViolet, colors.auroraBlue, 0.45f)
        else -> lerp(colors.auroraBlue, colors.nebula, 0.35f)
    }
    val lift = 0.24f + (colorSeed % 3) * 0.09f
    val top = lerp(colors.deepSurface, accent, lift)
    return listOf(top, colors.deepSurface, colors.spaceVoid.copy(alpha = 0.85f))
}

@Composable
fun GlassArtwork(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    colorSeed: Int = 0,
    shape: Shape = MaterialTheme.shapes.small,
    initials: String? = null,
    icon: ImageVector? = Icons.Filled.MusicNote,
) {
    val colors = CelestialTheme.colors
    Box(
        modifier = modifier
            .background(Brush.radialGradient(artworkColors(colorSeed, colors)), shape)
            .clip(shape)
            .size(size),
        contentAlignment = Alignment.Center,
    ) {
        when {
            initials != null && initials.isNotBlank() -> ArtworkInitials(initials)
            icon != null -> Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.starlight.copy(alpha = 0.55f),
            )
        }
    }
}

@Composable
private fun BoxScope.ArtworkInitials(initials: String) {
    val colors = CelestialTheme.colors
    Text(
        text = initials.take(2).uppercase(),
        style = MaterialTheme.typography.titleSmall,
        color = colors.starlight.copy(alpha = 0.75f),
        textAlign = TextAlign.Center,
        maxLines = 1,
    )
}
