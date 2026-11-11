package com.celestial.player.ui.screens.now_playing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.celestial.player.domain.model.Track
import com.celestial.player.playback.PlayerUiState
import com.celestial.player.playback.RepeatMode
import com.celestial.player.ui.components.backdrop.DeepSpaceBackdrop
import com.celestial.player.ui.components.glass.CelestialGlassSurface
import com.celestial.player.ui.components.glass.GlassIconButton
import com.celestial.player.ui.components.glass.GlassListRow
import com.celestial.player.ui.components.glass.GlassSectionHeader
import com.celestial.player.ui.components.player.AlbumPlanet3DView
import com.celestial.player.ui.components.player.AsteroidControlButton
import com.celestial.player.ui.components.player.SeekRingView
import com.celestial.player.ui.components.player.UfoPlayPauseButton
import com.celestial.player.ui.theme.CelestialTheme

@Composable
fun NowPlayingScreen(
    state: PlayerUiState,
    albumColorSeed: Int,
    isFavourite: Boolean,
    onDismiss: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleFavourite: () -> Unit,
    onSelectQueueTrack: (Track) -> Unit,
    modifier: Modifier = Modifier,
) {
    val track = state.currentTrack
    var queueOpen by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CelestialTheme.colors.spaceVoid),
    ) {
        DeepSpaceBackdrop(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = CelestialTheme.spacing.screenEdge),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = CelestialTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                GlassIconButton(
                    icon = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Dismiss Now Playing",
                    onClick = onDismiss,
                )
                Text(
                    text = "Now Playing",
                    style = MaterialTheme.typography.titleMedium,
                    color = CelestialTheme.colors.starlight,
                )
                GlassIconButton(
                    icon = Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = "Queue",
                    onClick = { queueOpen = !queueOpen },
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = CelestialTheme.spacing.lg),
            ) {
                SeekRingView(
                    progressFraction = state.progressFraction,
                    durationMs = state.durationMs,
                    onSeekTo = onSeekTo,
                    size = 280.dp,
                )
                AlbumPlanet3DView(
                    albumColorSeed = albumColorSeed,
                    isPlaying = state.isPlaying,
                    size = 210.dp,
                )
            }

            Spacer(modifier = Modifier.height(CelestialTheme.spacing.md))

            Text(
                text = track?.title ?: "No track selected",
                style = MaterialTheme.typography.headlineSmall,
                color = CelestialTheme.colors.starlight,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            Text(
                text = if (track != null) "${track.artistName} · ${track.albumTitle}" else "Celestial Player",
                style = MaterialTheme.typography.bodyMedium,
                color = CelestialTheme.colors.moonlight,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            val streamLabel = when {
                state.isPreparing || state.isBuffering -> "Preparing local file…"
                state.playbackError != null -> "Playback error · ${state.playbackError}"
                track != null -> if (state.isPlaying) "Playing from your library" else "Paused · local file"
                else -> null
            }
            if (streamLabel != null) {
                Text(
                    text = streamLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = CelestialTheme.colors.moonlight.copy(alpha = 0.8f),
                    maxLines = 1,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsteroidControlButton(
                    isNext = false,
                    onClick = onPrevious,
                    size = 58.dp,
                )
                UfoPlayPauseButton(
                    isPlaying = state.isPlaying,
                    onTogglePlayPause = onTogglePlayPause,
                    size = 80.dp,
                )
                AsteroidControlButton(
                    isNext = true,
                    onClick = onNext,
                    size = 58.dp,
                )
            }

            Spacer(modifier = Modifier.height(CelestialTheme.spacing.lg))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = CelestialTheme.spacing.lg),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlassIconButton(
                    icon = if (isFavourite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isFavourite) "Remove favourite" else "Mark favourite",
                    onClick = onToggleFavourite,
                )
                GlassIconButton(
                    icon = Icons.Filled.Shuffle,
                    contentDescription = "Shuffle",
                    selected = state.shuffleEnabled,
                    stateDescription = if (state.shuffleEnabled) "On" else "Off",
                    onClick = onToggleShuffle,
                )
                GlassIconButton(
                    icon = when (state.repeatMode) {
                        RepeatMode.One -> Icons.Filled.RepeatOne
                        else -> Icons.Filled.Repeat
                    },
                    contentDescription = "Repeat",
                    selected = state.repeatMode != RepeatMode.Off,
                    stateDescription = when (state.repeatMode) {
                        RepeatMode.Off -> "Off"
                        RepeatMode.All -> "All tracks"
                        RepeatMode.One -> "This track"
                    },
                    onClick = onCycleRepeat,
                )
            }
        }

        AnimatedVisibility(
            visible = queueOpen,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .fillMaxSize()
                .zIndex(10f),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CelestialTheme.colors.spaceVoid.copy(alpha = 0.7f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { queueOpen = false },
                    ),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = CelestialTheme.spacing.md, vertical = CelestialTheme.spacing.lg),
                ) {
                    CelestialGlassSurface(
                        modifier = Modifier.fillMaxWidth(),
                        selected = true,
                    ) {
                        Column(modifier = Modifier.padding(CelestialTheme.spacing.md)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                GlassSectionHeader(text = "Up Next (${state.queue.size})")
                                GlassIconButton(
                                    icon = Icons.Filled.Close,
                                    contentDescription = "Close Queue",
                                    onClick = { queueOpen = false },
                                )
                            }
                            LazyColumn(
                                contentPadding = PaddingValues(top = CelestialTheme.spacing.sm),
                                verticalArrangement = Arrangement.spacedBy(CelestialTheme.spacing.xs),
                            ) {
                            val queueList: List<Track> = state.queue
                            items(
                                items = queueList,
                                key = { track: Track -> track.id.value },
                                contentType = { "queue" },
                            ) { qTrack ->
                                    val isCurrent = qTrack.id == state.currentTrack?.id
                                    GlassListRow(
                                        title = qTrack.title,
                                        subtitle = qTrack.artistName,
                                        onClick = {
                                            onSelectQueueTrack(qTrack)
                                            queueOpen = false
                                        },
                                        selected = isCurrent,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
