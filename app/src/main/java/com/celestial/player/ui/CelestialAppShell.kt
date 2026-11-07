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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.material3.Text
import com.celestial.player.data.BackendStatus
import com.celestial.player.data.LibraryStateHolder
import com.celestial.player.domain.model.Track
import com.celestial.player.playback.PlayerController
import com.celestial.player.playback.PlayerUiState
import com.celestial.player.scene.formation.UniverseFormationSequence
import com.celestial.player.scene.host.CelestialUniverseScene
import com.celestial.player.scene.host.UniverseSceneState
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
import android.content.Context
import kotlinx.coroutines.delay

@Composable
fun CelestialAppShell(
    library: LibraryStateHolder,
    reducedEffects: Boolean,
    onReducedEffectsChange: (Boolean) -> Unit,
) {
    val nav = remember { CelestialNavState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val universeSceneState = remember { UniverseSceneState() }
    var settingsOpen by remember { mutableStateOf(false) }
    var planetHintVisible by remember { mutableStateOf(false) }

    val playerController = remember(context, scope, library.apiClient) {
        PlayerController(
            context = context,
            coroutineScope = scope,
            onTrackPlayed = { track -> library.recordPlayed(track) },
            apiClient = library.apiClient,
        )
    }
    DisposableEffect(playerController) {
        onDispose { playerController.release() }
    }

    val albums = library.albums
    val tracks = library.tracks
    val artists = library.artists

    val playerState = playerController.state
    val currentTrack = playerState.currentTrack
    val currentColorSeed = currentTrack?.let(library::colorSeedFor) ?: 0

    val prefs = remember(context) {
        context.getSharedPreferences("celestial_prefs", Context.MODE_PRIVATE)
    }
    var showFormation by remember {
        mutableStateOf(!prefs.getBoolean("intro_completed", false))
    }
    val dismissFormation = {
        showFormation = false
        prefs.edit().putBoolean("intro_completed", true).apply()
    }

    BackHandler(enabled = settingsOpen || !nav.isAtRoot() || showFormation) {
        if (showFormation) {
            dismissFormation()
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
            universeSceneState = universeSceneState,
        )

        if (showFormation) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(5f),
            ) {
                UniverseFormationSequence(
                    onFinished = dismissFormation,
                    reducedMotion = reducedEffects,
                )
            }
        }

        // The expanded player already shows the current track and controls, so the persistent mini
        // planet would only overlap its shuffle/repeat controls there.
        if (nav.current != NavTarget.NowPlaying) {
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
                        backendStatus = library.backendStatus,
                        backendStatusText = library.backendStatusText,
                        apiBaseUrl = library.apiClient.baseUrl,
                        isSyncing = library.isSyncing,
                        onSync = { library.sync(manual = true) },
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
    backendStatus: BackendStatus,
    backendStatusText: String,
    apiBaseUrl: String,
    isSyncing: Boolean,
    onSync: () -> Unit,
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
        title = "Account & sync",
        entries = listOf(
            SettingsEntryValue.Info(
                label = "Celestial server",
                description = backendStatusText,
                trailing = if (isSyncing) "Syncing…" else "Sync now",
                enabled = true,
                onClick = if (isSyncing) null else onSync,
            ),
            SettingsEntryValue.Info(
                label = "Connection mode",
                description = when (backendStatus) {
                    BackendStatus.Online -> "Live library · playlists, favourites and recents synced"
                    BackendStatus.Checking -> "Checking backend availability"
                    BackendStatus.Offline -> "Offline library · changes retry when the server returns"
                },
                trailing = when (backendStatus) {
                    BackendStatus.Online -> "Online"
                    BackendStatus.Checking -> "Checking"
                    BackendStatus.Offline -> "Offline"
                },
            ),
            SettingsEntryValue.Info(
                label = "API endpoint",
                description = apiBaseUrl,
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
    universeSceneState: UniverseSceneState,
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
                sceneState = universeSceneState,
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
                backendStatusText = library.backendStatusText,
                isBackendConnected = library.isBackendConnected,
                onRetrySync = { library.sync() },
                onOpenPlaylist = { nav.navigate(NavTarget.PlaylistDetail(it.id)) },
                onPlayPlaylist = { playlist ->
                    val plTracks = playlist.trackIds.mapNotNull { library.trackById(it) }
                    plTracks.firstOrNull()?.let { onPlay(it, plTracks) }
                },
                onCreatePlaylist = { name -> library.createPlaylist(name) },
                onBack = { nav.back() },
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
                        colorSeedFor = { track -> library.colorSeedFor(track) },
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
                colorSeedFor = { track -> library.colorSeedFor(track) },
                onPlayTrack = onPlay,
                onToggleFavourite = onToggleFavourite,
                onOpenAlbum = { nav.navigate(NavTarget.AlbumDetail(it.id)) },
                onOpenArtist = { nav.navigate(NavTarget.ArtistDetail(it.id)) },
                onOpenPlaylist = { nav.navigate(NavTarget.PlaylistDetail(it.id)) },
                onSearch = { query -> library.remoteSearch(query) },
                onBack = { nav.back() },
            )

            is NavTarget.AllSongs -> AllSongsScreen(
                tracks = tracks,
                isFavourite = { library.isFavourite(it.id) },
                currentTrackId = playerState.currentTrack?.id,
                colorSeedFor = { track -> library.colorSeedFor(track) },
                onPlayTrack = onPlay,
                onToggleFavourite = onToggleFavourite,
                onBack = { nav.back() },
            )

            is NavTarget.Albums -> AlbumsScreen(
                albums = albums,
                onOpenAlbum = { nav.navigate(NavTarget.AlbumDetail(it.id)) },
                onBack = { nav.back() },
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
                onBack = { nav.back() },
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
                colorSeedFor = { track -> library.colorSeedFor(track) },
                onPlay = onPlay,
                onToggleFavourite = onToggleFavourite,
                onBack = { nav.back() },
            )

            is NavTarget.RecentlyPlayed -> RecentlyPlayedScreen(
                recentlyPlayed = library.recents,
                trackFor = { library.trackById(it) },
                currentTrackId = playerState.currentTrack?.id,
                colorSeedFor = { track -> library.colorSeedFor(track) },
                onPlay = onPlay,
                onBack = { nav.back() },
            )
        }
    }
}