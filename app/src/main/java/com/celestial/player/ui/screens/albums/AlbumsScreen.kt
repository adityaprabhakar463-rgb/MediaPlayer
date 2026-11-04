package com.celestial.player.ui.screens.albums

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.celestial.player.domain.model.Album
import com.celestial.player.ui.components.glass.GlassArtwork
import com.celestial.player.ui.screens.common.DestinationScreen
import com.celestial.player.ui.theme.CelestialTheme

@Composable
fun AlbumsScreen(
    albums: List<Album>,
    onOpenAlbum: (Album) -> Unit,
    modifier: Modifier = Modifier,
) {
    DestinationScreen(
        title = "Albums",
        subtitle = "${albums.size} collections",
        modifier = modifier,
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = PaddingValues(
                start = CelestialTheme.spacing.screenEdge,
                end = CelestialTheme.spacing.screenEdge,
                top = CelestialTheme.spacing.sm,
                bottom = 140.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(CelestialTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(CelestialTheme.spacing.md),
        ) {
            items(items = albums, key = { it.id.value }, contentType = { "album" }) { album ->
                AlbumCard(album = album, onOpen = { onOpenAlbum(album) })
            }
        }
    }
}

@Composable
fun AlbumCard(
    album: Album,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = CelestialTheme.spacing
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onOpen,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GlassArtwork(
            size = 160.dp,
            colorSeed = album.colorSeed,
            shape = MaterialTheme.shapes.large,
            icon = Icons.Filled.Album,
        )
        Spacer(modifier = Modifier.height(spacing.sm))
        Text(
            text = album.title,
            style = MaterialTheme.typography.titleSmall,
            color = CelestialTheme.colors.starlight,
            maxLines = 1,
        )
        Text(
            text = album.artistName,
            style = MaterialTheme.typography.bodySmall,
            color = CelestialTheme.colors.moonlight,
            maxLines = 1,
        )
    }
}
