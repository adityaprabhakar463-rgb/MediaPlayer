package com.celestial.player.ui.components.glass

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.celestial.player.domain.model.Track

@Composable
fun GlassTrackRow(
    track: Track,
    subtitle: String = track.artistName,
    colorSeed: Int = track.albumId.value.hashCode(),
    isFavourite: Boolean = false,
    isCurrent: Boolean = false,
    onToggleFavourite: (() -> Unit)? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassListRow(
        title = track.title,
        subtitle = subtitle,
        selected = isCurrent,
        onClick = onClick,
        modifier = modifier,
        leading = {
            GlassArtwork(
                size = 48.dp,
                colorSeed = colorSeed,
            )
        },
        trailing = {
            if (onToggleFavourite != null) {
                GlassIconButton(
                    icon = if (isFavourite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isFavourite) "Remove from favourites" else "Add to favourites",
                    selected = isFavourite,
                    onClick = onToggleFavourite,
                )
            }
        },
    )
}
