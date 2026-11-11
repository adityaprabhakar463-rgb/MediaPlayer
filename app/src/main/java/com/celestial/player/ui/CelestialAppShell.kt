package com.celestial.player.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.celestial.player.data.LibraryStateHolder
import com.celestial.player.data.LibraryStatus
import com.celestial.player.data.local.LibraryPreferences
import com.celestial.player.domain.model.Track
import com.celestial.player.playback.PlayerController
import com.celestial.player.playback.PlayerUiState
import com.celestial.player.scene.formation.UniverseFormationSequence
import com.celestial.player.scene.host.CelestialUniverseScene
import com.celestial.player.scene.host.UniverseSceneState
import com.celestial.player.ui.components.glass.CelestialGlassSurface
import com.celestial.player.ui.components.glass.GlassSettingsPanel
import com.celestial.player.ui.components.glass.SettingsEntryValue
import com.celestial.player.ui.components.glass.SettingsSection
import com.celestial.player.ui.components.library.LibraryScanPill
import com.celestial.player.ui.components.library.MusicFolderPrompt
import com.celestial.player.ui.components.player.MiniPlayerPlanet
import com.celestial.player.ui.navigation.CelestialNavState
import com.celestial.player.ui.navigation.NavTarget
import com.celestial.player.ui.screens.favourites.FavouritesScreen
import com.celestial.player.ui.screens.library.AllSongsScreen
import com.celestial.player.ui.screens.now_playing.NowPlayingScreen
import com.celestial.player.ui.screens.playlists.PlaylistDetailScreen
import com.celestial.player.ui.screens.playlists.PlaylistsScreen
import com.celestial.player.ui.screens.recentplay.RecentlyPlayedScreen
import com.celestial.player.ui.screens.search.SearchDestinationScreen
import com.celestial.player.ui.theme.CelestialTheme

@Composable
fun CelestialAppShell(
    library: LibraryStateHolder,
    playerController: PlayerController,
    navState: CelestialNavState,
    reducedEffects: Boolean,
    onReducedEffectsChange: (Boolean) -> Unit,
) {
    val nav = navState
    val context = LocalContext.current
    val universeSceneState = remember { UniverseSceneState() }
    val preferences = remember(context) { LibraryPreferences(context.applicationContext) }
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    var folderPromptDismissed by rememberSaveable { mutableStateOf(false) }

    val folderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        if (uri != null) {
            folderPromptDismissed = true
            library.selectFolder(uri)
        }
    }

    val tracks = library.tracks

    val playerState = playerController.state
    val currentTrack = playerState.currentTrack
    val currentColorSeed = currentTrack?.let(library::colorSeedFor) ?: 0
    val currentArtworkPath = currentTrack?.let(library::artworkPathFor)

    // Removing the library folder invalidates every queued SAF URI, so stop instead of erroring.
    LaunchedEffect(library.hasFolder) {
        if (!library.hasFolder) playerController.stop()
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* Playback works without it; the notification may be hidden. */ }
    LaunchedEffect(currentTrack != null) {
        if (currentTrack != null &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var showFormation by rememberSaveable {
        mutableStateOf(!preferences.introCompleted)
    }
    val dismissFormation = {
        showFormation = false
        preferences.introCompleted = true
    }

    val folderPromptVisible = !showFormation && !library.hasFolder && !folderPromptDismissed

    BackHandler(
        enabled = settingsOpen || !nav.isAtRoot() || showFormation || folderPromptVisible,
    ) {
        when {
            showFormation -> dismissFormation()
            folderPromptVisible -> folderPromptDismissed = true
            settingsOpen -> settingsOpen = false
            else -> nav.back()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        DestinationContent(
            nav = nav,
            tracks = tracks,
            library = library,
            playerState = playerState,
            playerController = playerController,
            currentColorSeed = currentColorSeed,
            currentArtworkPath = currentArtworkPath,
            onPlay = { track, queue -> playerController.play(track, queue) },
            onToggleFavourite = { library.toggleFavourite(it.id) },
            onOpenSettings = { settingsOpen = true },
            onChooseFolder = { folderPicker.launch(null) },
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

        if (library.isScanning && !showFormation) {
            LibraryScanPill(
                statusText = library.statusText,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(2f)
                    .statusBarsPadding()
                    .padding(top = CelestialTheme.spacing.sm),
            )
        }

        // The expanded player already shows the current track and controls, so the persistent mini
        // planet would only overlap its shuffle/repeat controls there.
        if (nav.current != NavTarget.NowPlaying) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .zIndex(2f)
                    .navigationBarsPadding()
                    .padding(end = 20.dp, bottom = 8.dp),
            ) {
                MiniPlayerPlanet(
                    state = playerState,
                    albumColorSeed = currentColorSeed,
                    albumArtworkPath = currentArtworkPath,
                    planetSize = 64.dp,
                    onClick = { nav.navigate(NavTarget.NowPlaying) },
                )
            }
        }

        if (folderPromptVisible) {
            MusicFolderPrompt(
                isScanning = library.isScanning,
                statusText = library.statusText,
                onChooseFolder = { folderPicker.launch(null) },
                onDismiss = { folderPromptDismissed = true },
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(4f),
            )
        }

        if (settingsOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(3f)
                    .background(CelestialTheme.colors.spaceVoid.copy(alpha = 0.55f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
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
                        folderName = library.folderName,
                        hasFolder = library.hasFolder,
                        trackCount = library.tracks.size,
                        statusText = library.statusText,
                        isScanning = library.isScanning,
                        onChooseFolder = { folderPicker.launch(null) },
                        onRescan = { library.rescan() },
                    ),
                    onDismiss = { settingsOpen = false },
                )
            }
        }
    }
}

@Composable
private fun settingsSections(
    reducedEffects: Boolean,
    onReducedEffectsChange: (Boolean) -> Unit,
    onReplayIntro: () -> Unit,
    folderName: String?,
    hasFolder: Boolean,
    trackCount: Int,
    statusText: String,
    isScanning: Boolean,
    onChooseFolder: () -> Unit,
    onRescan: () -> Unit,
): List<SettingsSection> = buildList {
    add(
        SettingsSection(
            title = "Library",
            entries = buildList {
                add(
                    SettingsEntryValue.Info(
                        label = "Music folder",
                        description = folderName ?: "No folder selected",
                        trailing = if (hasFolder) "Change" else "Choose",
                        enabled = true,
                        onClick = onChooseFolder,
                    ),
                )
                add(
                    SettingsEntryValue.Info(
                        label = "Library status",
                        description = statusText,
                        trailing = when {
                            isScanning -> "Scanning…"
                            hasFolder -> "Rescan"
                            else -> null
                        },
                        onClick = if (hasFolder && !isScanning) onRescan else null,
                    ),
                )
                add(
                    SettingsEntryValue.Info(
                        label = "Indexed tracks",
                        description = if (folderName != null) {
                            "$trackCount audio files in $folderName"
                        } else {
                            "Choose a folder to build your library"
                        },
                        trailing = trackCount.toString(),
                    ),
                )
            },
        ),
    )
    add(
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
    )
    add(
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
    )
    add(
        SettingsSection(
            title = "About",
            entries = listOf(
                SettingsEntryValue.Info(
                    label = "Celestial",
                    description = "Offline space-themed music player for Android",
                    trailing = "v0.2.0",
                    enabled = true,
                ),
            ),
        ),
    )
}

@Composable
private fun DestinationContent(
    nav: CelestialNavState,
    tracks: List<Track>,
    library: LibraryStateHolder,
    playerState: PlayerUiState,
    playerController: PlayerController,
    currentColorSeed: Int,
    currentArtworkPath: String?,
    onPlay: (Track, List<Track>) -> Unit,
    onToggleFavourite: (Track) -> Unit,
    onOpenSettings: () -> Unit,
    onChooseFolder: () -> Unit,
    universeSceneState: UniverseSceneState,
) {
    val reducedMotion = CelestialTheme.effects.reducedMotion
    AnimatedContent(
        targetState = nav.current,
        transitionSpec = {
            if (reducedMotion) {
                fadeIn(androidx.compose.animation.core.tween(160)) togetherWith
                    fadeOut(androidx.compose.animation.core.tween(160))
            } else {
                (slideInVertically(
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
                onSelectPlanet = { destination -> nav.navigate(destination) },
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
                libraryStatusText = library.statusText,
                isLibraryReady = library.status == LibraryStatus.Ready && library.tracks.isNotEmpty(),
                onRescan = { library.rescan() },
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
                playlists = library.playlists,
                suggestedTrackIds = tracks.take(5).map { it.id },
                isFavourite = { library.isFavourite(it.id) },
                currentTrackId = playerState.currentTrack?.id,
                colorSeedFor = { track -> library.colorSeedFor(track) },
                onPlayTrack = onPlay,
                onToggleFavourite = onToggleFavourite,
                onOpenPlaylist = { nav.navigate(NavTarget.PlaylistDetail(it.id)) },
                onSearch = { query -> library.search(query) },
                onBack = { nav.back() },
            )

            is NavTarget.AllSongs -> AllSongsScreen(
                tracks = tracks,
                libraryStatusText = library.statusText,
                hasFolder = library.hasFolder,
                isScanning = library.isScanning,
                isFavourite = { library.isFavourite(it.id) },
                currentTrackId = playerState.currentTrack?.id,
                colorSeedFor = { track -> library.colorSeedFor(track) },
                onChooseFolder = onChooseFolder,
                onRescan = { library.rescan() },
                onPlayTrack = onPlay,
                onToggleFavourite = onToggleFavourite,
                onBack = { nav.back() },
            )

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
