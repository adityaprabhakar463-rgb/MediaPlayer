package com.celestial.player.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.material3.Text
import com.celestial.player.data.LibraryStateHolder
import com.celestial.player.data.SampleLibrary
import com.celestial.player.domain.model.Track
import com.celestial.player.playback.PlayerController
import com.celestial.player.playback.PlayerUiState
import com.celestial.player.scene.formation.UniverseFormationSequence
import com.celestial.player.scene.host.CelestialUniverseScene
import com.celestial.player.ui.components.glass.CelestialGlassSurface
import com.celestial.player.ui.components.player.MiniPlayerPlanet
import com.celestial.player.ui.components.glass.GlassSettingsPanel
import com.celestial.player.ui.components.glass.SettingsEntryValue
import com.celestial.player.ui.components.glass.SettingsSection
import com.celestial.player.ui.navigation.CelestialNavState
import com.celestial.player.ui.navigation.NavTarget
import com.celestial.player.ui.screens.albums.AlbumDetailScreen
import com.celestial.player.ui.screens.albums.AlbumsScreen
import com.celestial.player.ui.screens.artists.ArtistDetailScreen
import com.celestial.player.ui.screens.artists.ArtistsScreen
import com.celestial.player.ui.screens.favourites.FavouritesScreen
import com.celestial.player.ui.screens.library.AllSongsScreen
import com.celestial.player.ui.screens.now_playing.NowPlayingScreen
import com.celestial.player.ui.screens.playlists.PlaylistDetailScreen
import com.celestial.player.ui.screens.playlists.PlaylistsScreen
import com.celestial.player.ui.screens.recentplay.RecentlyPlayedScreen
import com.celestial.player.ui.screens.search.SearchDestinationScreen
import com.celestial.player.ui.screens.universe.UniversePlaceholderScreen
import com.celestial.player.ui.theme.CelestialTheme
import kotlinx.coroutines.delay

@Composable
fun CelestialAppShell(
    library: LibraryStateHolder,
    reducedEffects: Boolean,
    onReducedEffectsChange: (Boolean) -> Unit,
) {
    val nav = remember { CelestialNavState() }
    val scope = rememberCoroutineScope()
    var settingsOpen by remember { mutableStateOf(false) }
    var planetHintVisible by remember { mutableStateOf(false) }

    val playerController = remember(scope) {
        PlayerController(
            coroutineScope = scope,
            onTrackPlayed = { track -> library.recordPlayed(track) },
        )
    }

    val albums = SampleLibrary.albums
    val tracks = SampleLibrary.tracks
    val artists = SampleLibrary.artists

    val playerState = playerController.state
    val currentTrack = playerState.currentTrack
    val currentColorSeed = remember(currentTrack?.albumId) {
        currentTrack?.albumId?.let { albumId -> albums.firstOrNull { it.id == albumId }?.colorSeed } ?: 0
    }

    var showFormation by remember { mutableStateOf(true) }

    BackHandler(enabled = settingsOpen || !nav.isAtRoot() || showFormation) {
        if (showFormation) {
            showFormation = false
        } else if (settingsOpen) {
            settingsOpen = false
        } else {
            nav.back()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        DestinationContent(
            nav = nav,
            tracks = tracks,
            albums = albums,
            artists = artists,
            library = library,
            playerState = playerState,
            playerController = playerController,
            currentColorSeed = currentColorSeed,
            onPlay = { track, queue -> playerController.play(track, queue) },
            onToggleFavourite = { library.toggleFavourite(it.id) },
            onOpenSettings = { settingsOpen = true },
        )

        if (showFormation) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(5f),
            ) {
                UniverseFormationSequence(
                    onFinished = { showFormation = false },
                    reducedMotion = reducedEffects,
                )
            }
        }

        AnimationLayerWrap(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .zIndex(2f)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 8.dp),
        ) {
            MiniPlayerPlanet(
                state = playerState,
                albumColorSeed = currentColorSeed,
                planetSize = 64.dp,
                onClick = { nav.navigate(NavTarget.NowPlaying) },
            )
        }

        if (settingsOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(3f)
                    .background(CelestialTheme.colors.spaceVoid.copy(alpha = 0.55f))
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = { settingsOpen = false },
                    ),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .zIndex(4f)
                    .padding(horizontal = 20.dp),
            ) {
                GlassSettingsPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    sections = settingsSections(
                        reducedEffects = reducedEffects,
                        onReducedEffectsChange = onReducedEffectsChange,
                        onReplayIntro = {
                            settingsOpen = false
                            showFormation = true
                        },
                    ),
                    onDismiss = { settingsOpen = false },
                )
            }
        }
    }
}

@Composable
private fun PlanetHintChip(hintVisible: Boolean) {
    val colors = CelestialTheme.colors
    androidx.compose.animation.AnimatedVisibility(visible = hintVisible) {
        CelestialGlassSurface(selected = true) {
                        Text(
                text = "Now Playing opens with the 3D universe",
                color = colors.starlight,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Composable
private fun AnimationLayerWrap(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) { content() }
}

@Composable
private fun settingsSections(
    reducedEffects: Boolean,
    onReducedEffectsChange: (Boolean) -> Unit,
    onReplayIntro: () -> Unit,
): List<SettingsSection> = listOf(
    SettingsSection(
        title = "Universe",
        entries = listOf(
            SettingsEntryValue.Toggle(
                label = "Reduced effects",
                description = "Calms ambient stars, comets and motion",
                checked = reducedEffects,
                onCheckedChange = onReducedEffectsChange,
            ),
        ),
    ),
    SettingsSection(
        title = "Playback",
        entries = listOf(
            SettingsEntryValue.Info(
                label = "Replay intro",
                description = "Replays the universe creation sequence",
                trailing = "Replay",
                enabled = true,
                onClick = onReplayIntro,
            ),
        ),
    ),
    SettingsSection(
        title = "About",
        entries = listOf(
            SettingsEntryValue.Info(
                label = "Celestial",
                description = "Space-themed media player for Android",
                trailing = "v0.1.0",
                enabled = true,
            ),
        ),
    ),
)

@Composable
private fun DestinationContent(
    nav: CelestialNavState,
    tracks: List<Track>,
    albums: List<com.celestial.player.domain.model.Album>,
    artists: List<com.celestial.player.domain.model.Artist>,
    library: LibraryStateHolder,
    playerState: PlayerUiState,
    playerController: PlayerController,
    currentColorSeed: Int,
    onPlay: (Track, List<Track>) -> Unit,
    onToggleFavourite: (Track) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val reducedMotion = CelestialTheme.effects.reducedMotion
    AnimatedContent(
        targetState = nav.current,
        transitionSpec = {
            if (reducedMotion) {
                fadeIn(androidx.compose.animation.core.tween(160)) togetherWith
                    fadeOut(androidx.compose.animation.core.tween(160))
            } else {                (slideInVertically(
                    animationSpec = androidx.compose.animation.core.tween(240),
                    initialOffsetY = { it / 16 },
                ) + fadeIn(androidx.compose.animation.core.tween(240))) togetherWith
                    (slideOutVertically(
                        animationSpec = androidx.compose.animation.core.tween(200),
                        targetOffsetY = { -it / 24 },
                    ) + fadeOut(androidx.compose.animation.core.tween(200)))
            }
        },
        label = "destinationTransition",
    ) { target ->
        when (target) {
            is NavTarget.Universe -> CelestialUniverseScene(
                onSelectPlanet = { target -> nav.navigate(target) },
                onOpenSettings = onOpenSettings,
                returningFromTarget = nav.previousDestination,
            )

            is NavTarget.NowPlaying -> NowPlayingScreen(
                state = playerState,
                albumColorSeed = currentColorSeed,
                isFavourite = playerState.currentTrack?.let { library.isFavourite(it.id) } ?: false,
                onDismiss = { nav.back() },
                onTogglePlayPause = { playerController.togglePlayPause() },
                onNext = { playerController.next() },
                onPrevious = { playerController.previous() },
                onSeekTo = { playerController.seekTo(it) },
                onToggleShuffle = { playerController.setShuffle(!playerState.shuffleEnabled) },
                onCycleRepeat = { playerController.cycleRepeatMode() },
                onToggleFavourite = { playerState.currentTrack?.let { library.toggleFavourite(it.id) } },
                onSelectQueueTrack = { playerController.play(it, playerState.queue) },
            )

            is NavTarget.Playlists -> PlaylistsScreen(
                playlists = library.playlists,
                onOpenPlaylist = { nav.navigate(NavTarget.PlaylistDetail(it.id)) },
                onPlayPlaylist = { playlist ->
                    val plTracks = playlist.trackIds.mapNotNull { library.trackById(it) }
                    plTracks.firstOrNull()?.let { onPlay(it, plTracks) }
                },
                onCreatePlaylist = { name -> library.createPlaylist(name) },
            )

            is NavTarget.PlaylistDetail -> {
                val playlist = library.playlistById(target.playlistId)
                if (playlist == null) {
                    nav.back()
                } else {
                    val plTracks = playlist.trackIds.mapNotNull { library.trackById(it) }
                    PlaylistDetailScreen(
                        playlist = playlist,
                        tracks = plTracks,
                        isFavourite = { library.isFavourite(it.id) },
                        currentTrackId = playerState.currentTrack?.id,
                        onBack = { nav.back() },
                        onPlay = onPlay,
                        onToggleFavourite = onToggleFavourite,
                        colorSeedFor = { track -> sampleSeedFor(track) },
                    )
                }
            }

            is NavTarget.Search -> SearchDestinationScreen(
                sampleTracks = tracks,
                sampleAlbums = albums,
                sampleArtists = artists,
                playlists = library.playlists,
                suggestedTrackIds = tracks.take(5).map { it.id },
                isFavourite = { library.isFavourite(it.id) },
                currentTrackId = playerState.currentTrack?.id,
                colorSeedFor = { track -> sampleSeedFor(track) },
                onPlayTrack = onPlay,
                onToggleFavourite = onToggleFavourite,
                onOpenAlbum = { nav.navigate(NavTarget.AlbumDetail(it.id)) },
                onOpenArtist = { nav.navigate(NavTarget.ArtistDetail(it.id)) },
                onOpenPlaylist = { nav.navigate(NavTarget.PlaylistDetail(it.id)) },
            )

            is NavTarget.AllSongs -> AllSongsScreen(
                tracks = tracks,
                isFavourite = { library.isFavourite(it.id) },
                currentTrackId = playerState.currentTrack?.id,
                colorSeedFor = { track -> sampleSeedFor(track) },
                onPlayTrack = onPlay,
                onToggleFavourite = onToggleFavourite,
            )

            is NavTarget.Albums -> AlbumsScreen(
                albums = albums,
                onOpenAlbum = { nav.navigate(NavTarget.AlbumDetail(it.id)) },
            )

            is NavTarget.AlbumDetail -> {
                val album = albums.firstOrNull { it.id == target.albumId }
                if (album == null) {
                    nav.back()
                } else {
                    val albumTracks = album.trackIds.mapNotNull { tracks.firstOrNull { t -> t.id == it } }
                    AlbumDetailScreen(
                        album = album,
                        tracks = albumTracks,
                        isFavourite = { library.isFavourite(it.id) },
                        currentTrackId = playerState.currentTrack?.id,
                        onBack = { nav.back() },
                        onPlay = onPlay,
                        onToggleFavourite = onToggleFavourite,
                    )
                }
            }

            is NavTarget.Artists -> ArtistsScreen(
                artists = artists,
                onOpenArtist = { nav.navigate(NavTarget.ArtistDetail(it.id)) },
            )

            is NavTarget.ArtistDetail -> {
                val artist = artists.firstOrNull { it.id == target.artistId }
                if (artist == null) {
                    nav.back()
                } else {
                    val artistTracks = artist.trackIds.mapNotNull { tracks.firstOrNull { t -> t.id == it } }
                    ArtistDetailScreen(
                        artist = artist,
                        tracks = artistTracks,
                        albums = albums.filter { it.artistId == artist.id },
                        isFavourite = { library.isFavourite(it.id) },
                        currentTrackId = playerState.currentTrack?.id,
                        onBack = { nav.back() },
                        onPlay = onPlay,
                        onToggleFavourite = onToggleFavourite,
                        onOpenAlbum = { nav.navigate(NavTarget.AlbumDetail(it.id)) },
                    )
                }
            }

            is NavTarget.Favourites -> FavouritesScreen(
                tracks = library.favouriteTracks(),
                currentTrackId = playerState.currentTrack?.id,
                colorSeedFor = { track -> sampleSeedFor(track) },
                onPlay = onPlay,
                onToggleFavourite = onToggleFavourite,
            )

            is NavTarget.RecentlyPlayed -> RecentlyPlayedScreen(
                recentlyPlayed = library.recents,
                trackFor = { library.trackById(it) },
                currentTrackId = playerState.currentTrack?.id,
                colorSeedFor = { track -> sampleSeedFor(track) },
                onPlay = onPlay,
            )
        }
    }
}

private fun sampleSeedFor(track: Track): Int =
    SampleLibrary.albums.firstOrNull { it.id == track.albumId }?.colorSeed
        ?: track.albumId.value.hashCode()
