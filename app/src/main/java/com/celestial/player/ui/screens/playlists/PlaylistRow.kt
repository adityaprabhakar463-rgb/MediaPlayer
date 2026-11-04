package com.celestial.player.ui.screens.playlists

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.celestial.player.domain.model.Playlist
import com.celestial.player.ui.components.glass.GlassArtwork
import com.celestial.player.ui.components.glass.GlassIconButton
import com.celestial.player.ui.components.glass.GlassListRow
import com.celestial.player.ui.theme.CelestialTheme

@Composable
fun PlaylistRow(
    playlist: Playlist,
    onOpen: () -> Unit,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }
    GlassListRow(
        title = playlist.name,
        subtitle = "${playlist.trackIds.size} tracks",
        onClick = onOpen,
        modifier = modifier,
        leading = {
            GlassArtwork(
                size = 52.dp,
                colorSeed = playlist.id.value.hashCode(),
                icon = Icons.AutoMirrored.Filled.QueueMusic,
            )
        },
        trailing = {
            Box {
                GlassIconButton(
                    icon = Icons.Filled.MoreVert,
                    contentDescription = "Playlist actions",
                    onClick = { menuOpen = true },
                )
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Play playlist") },
                        onClick = { menuOpen = false; onPlay() },
                    )
                    DropdownMenuItem(
                        text = { Text("Open playlist") },
                        onClick = { menuOpen = false; onOpen() },
                    )
                }
            }
        },
    )
}
