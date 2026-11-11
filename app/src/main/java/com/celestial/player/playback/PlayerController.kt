package com.celestial.player.playback

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.celestial.player.domain.model.Track
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

/**
 * Compose-facing playback façade over a Media3 [MediaController].
 *
 * The actual player lives in [PlaybackService]; this class translates controller events into
 * [PlayerUiState], and forwards user commands. The queue is mirrored from the controller so the app
 * restores the correct track after process/Activity recreation.
 */
class PlayerController(
    context: Context,
    private val coroutineScope: CoroutineScope,
    private val onTrackPlayed: (Track) -> Unit = {},
    private val trackResolver: (String) -> Track? = { null },
) {
    var state by mutableStateOf(PlayerUiState())
        private set

    private val appContext = context.applicationContext
    private val ready = CompletableDeferred<MediaController?>()
    private var controller: MediaController? = null
    private var controllerFuture: com.google.common.util.concurrent.ListenableFuture<MediaController>? = null
    private var tickJob: Job? = null
    private var lastReportedTrackId: String? = null

    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            syncFromController()
            mediaItem?.mediaId?.let(::reportPlayed)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            state = state.copy(isPlaying = isPlaying)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            state = when (playbackState) {
                Player.STATE_BUFFERING -> state.copy(
                    isBuffering = true,
                    isPreparing = state.currentTrack != null && state.positionMs == 0L,
                )

                Player.STATE_READY -> state.copy(
                    isBuffering = false,
                    isPreparing = false,
                    durationMs = controller?.duration?.takeIf { it > 0L } ?: state.durationMs,
                )

                Player.STATE_ENDED -> state.copy(isBuffering = false, isPreparing = false)
                else -> state
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            state = state.copy(
                isPlaying = false,
                isBuffering = false,
                isPreparing = false,
                playbackError = error.message ?: "Could not play this file",
            )
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            state = state.copy(shuffleEnabled = shuffleModeEnabled)
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            state = state.copy(repeatMode = repeatMode.toRepeatMode())
        }
    }

    init {
        val token = SessionToken(appContext, ComponentName(appContext, PlaybackService::class.java))
        val future = MediaController.Builder(appContext, token).buildAsync()
        controllerFuture = future
        future.addListener(
            {
                val connected = runCatching { future.get() }.getOrNull()
                if (connected == null) {
                    ready.complete(null)
                    state = state.copy(playbackError = "Playback service unavailable")
                    return@addListener
                }
                controller = connected
                connected.addListener(playerListener)
                ready.complete(connected)
                syncFromController()
                startTicker()
            },
            ContextCompat.getMainExecutor(appContext),
        )
    }

    fun play(track: Track, queue: List<Track> = listOf(track)) {
        val playQueue = queue.ifEmpty { listOf(track) }
        val index = playQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        state = PlayerUiState(
            currentTrack = track,
            isPlaying = true,
            positionMs = 0L,
            durationMs = track.durationMs,
            queue = playQueue,
            queueIndex = index,
            shuffleEnabled = state.shuffleEnabled,
            repeatMode = state.repeatMode,
            isPreparing = true,
        )
        reportPlayed(track.id.value, force = true)
        withController { controller ->
            controller.setMediaItems(playQueue.map(::toMediaItem), index, 0L)
            controller.prepare()
            controller.play()
        }
    }

    fun togglePlayPause() {
        val track = state.currentTrack ?: return
        val nextPlaying = !state.isPlaying
        state = state.copy(isPlaying = nextPlaying, playbackError = null)
        withController { controller ->
            if (nextPlaying) {
                if (controller.mediaItemCount == 0) {
                    play(track, state.queue)
                } else {
                    if (controller.playbackState == Player.STATE_ENDED) controller.seekTo(0L)
                    controller.play()
                }
            } else {
                controller.pause()
            }
        }
    }

    fun next() {
        withController { controller ->
            if (controller.hasNextMediaItem()) controller.seekToNextMediaItem()
            else if (controller.repeatMode == Player.REPEAT_MODE_ALL && controller.mediaItemCount > 0) {
                controller.seekTo(0, 0L)
            }
        }
    }

    fun previous() {
        withController { controller ->
            if (controller.currentPosition > RESTART_THRESHOLD_MS) {
                controller.seekTo(0L)
            } else if (controller.hasPreviousMediaItem()) {
                controller.seekToPreviousMediaItem()
            } else {
                controller.seekTo(0L)
            }
        }
    }

    fun seekTo(positionMs: Long) {
        val bounded = positionMs.coerceIn(0L, state.durationMs)
        state = state.copy(positionMs = bounded)
        withController { it.seekTo(bounded) }
    }

    fun setShuffle(enabled: Boolean) {
        state = state.copy(shuffleEnabled = enabled)
        withController { it.shuffleModeEnabled = enabled }
    }

    fun cycleRepeatMode() {
        val next = when (state.repeatMode) {
            RepeatMode.Off -> RepeatMode.All
            RepeatMode.All -> RepeatMode.One
            RepeatMode.One -> RepeatMode.Off
        }
        state = state.copy(repeatMode = next)
        withController { it.repeatMode = next.toPlayerRepeatMode() }
    }

    fun release() {
        tickJob?.cancel()
        tickJob = null
        controller?.removeListener(playerListener)
        controller = null
        controllerFuture?.let(MediaController::releaseFuture)
    }

    /** Clears playback and the queue, used when the library folder is removed. */
    fun stop() {
        lastReportedTrackId = null
        state = PlayerUiState()
        withController { controller ->
            controller.stop()
            controller.clearMediaItems()
        }
    }

    private fun syncFromController() {
        val controller = controller ?: return
        val count = controller.mediaItemCount
        if (count == 0) return
        val tracks = (0 until count).mapNotNull { index ->
            trackResolver(controller.getMediaItemAt(index).mediaId)
        }
        if (tracks.size != count) return

        val index = controller.currentMediaItemIndex.coerceIn(0, tracks.lastIndex)
        val current = tracks[index]
        val duration = controller.duration.takeIf { it > 0L } ?: current.durationMs
        state = state.copy(
            currentTrack = current,
            isPlaying = controller.isPlaying,
            positionMs = controller.currentPosition.coerceIn(0L, duration.coerceAtLeast(0L)),
            durationMs = duration,
            isBuffering = controller.playbackState == Player.STATE_BUFFERING,
            queue = tracks,
            queueIndex = index,
            shuffleEnabled = controller.shuffleModeEnabled,
            repeatMode = controller.repeatMode.toRepeatMode(),
        )
    }

    private fun startTicker() {
        if (tickJob?.isActive == true) return
        tickJob = coroutineScope.launch {
            while (isActive) {
                delay(TICK_MS)
                val controller = controller ?: continue
                if (state.currentTrack == null && controller.currentMediaItem != null) {
                    syncFromController()
                }
                if (state.currentTrack == null) continue
                val duration = controller.duration.takeIf { it > 0L } ?: state.durationMs
                val position = controller.currentPosition.coerceIn(0L, duration.coerceAtLeast(0L))
                state = state.copy(
                    positionMs = position,
                    durationMs = duration,
                    isPlaying = controller.isPlaying,
                    isBuffering = controller.playbackState == Player.STATE_BUFFERING,
                )
            }
        }
    }

    private fun reportPlayed(mediaId: String, force: Boolean = false) {
        if (mediaId == lastReportedTrackId && !force) return
        lastReportedTrackId = mediaId
        trackResolver(mediaId)?.let(onTrackPlayed)
    }

    private fun toMediaItem(track: Track): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artistName)
            .setAlbumTitle(track.albumTitle)
            .apply {
                track.artworkPath?.let { path -> setArtworkUri(Uri.fromFile(File(path))) }
            }
            .build()
        return MediaItem.Builder()
            .setMediaId(track.id.value)
            .setUri(track.mediaUri)
            .setMediaMetadata(metadata)
            .build()
    }

    private fun withController(block: (MediaController) -> Unit) {
        coroutineScope.launch {
            val controller = controller ?: ready.await() ?: return@launch
            block(controller)
        }
    }

    private fun Int.toRepeatMode(): RepeatMode = when (this) {
        Player.REPEAT_MODE_ALL -> RepeatMode.All
        Player.REPEAT_MODE_ONE -> RepeatMode.One
        else -> RepeatMode.Off
    }

    private fun RepeatMode.toPlayerRepeatMode(): Int = when (this) {
        RepeatMode.Off -> Player.REPEAT_MODE_OFF
        RepeatMode.All -> Player.REPEAT_MODE_ALL
        RepeatMode.One -> Player.REPEAT_MODE_ONE
    }

    private companion object {
        const val TICK_MS = 500L
        const val RESTART_THRESHOLD_MS = 3_000L
    }
}
