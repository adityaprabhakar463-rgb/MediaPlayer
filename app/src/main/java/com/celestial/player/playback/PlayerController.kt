package com.celestial.player.playback

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.Player
import com.celestial.player.data.remote.ApiException
import com.celestial.player.data.remote.CelestialApiClient
import com.celestial.player.domain.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlayerController(
    context: Context,
    initMsPerTick: Long = 500L,
    private val onTrackPlayed: (Track) -> Unit = {},
    private val coroutineScope: CoroutineScope,
    private val apiClient: CelestialApiClient,
) {

    var state by mutableStateOf(PlayerUiState())
        private set

    private val msPerTick = initMsPerTick
    private var tickJob: Job? = null
    private var resolveJob: Job? = null

    private val audioEngine: AudioEngine

    init {
        audioEngine = AudioEngine(
            context = context.applicationContext,
            listener = object : AudioEngine.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (state.currentTrack != null && audioEngine.hasMedia) {
                        state = state.copy(isPlaying = isPlaying)
                    }
                }

                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_BUFFERING -> {
                            this@PlayerController.state = this@PlayerController.state.copy(isBuffering = true)
                        }

                        Player.STATE_READY -> {
                            val duration = audioEngine.durationMs.takeIf { it > 0L }
                            this@PlayerController.state = this@PlayerController.state.copy(
                                isBuffering = false,
                                durationMs = duration ?: this@PlayerController.state.durationMs,
                            )
                        }

                        Player.STATE_ENDED -> {
                            this@PlayerController.state = this@PlayerController.state.copy(isBuffering = false)
                            handleEnded()
                        }
                    }
                }

                override fun onError(message: String) {
                    Log.w(TAG, "engine error: $message, falling back to local ticker")
                    // Fall back gracefully to local position ticker if media engine encounters stream error
                    state = state.copy(
                        isBuffering = false,
                        isResolvingStream = false,
                        playbackError = null,
                    )
                }
            },
        )
        startTicker()
    }

    fun play(track: Track, queue: List<Track> = listOf(track)) {
        audioEngine.stop()
        val index = queue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        state = PlayerUiState(
            currentTrack = track,
            isPlaying = true,
            positionMs = 0L,
            durationMs = track.durationMs,
            queue = queue,
            queueIndex = index,
            shuffleEnabled = state.shuffleEnabled,
            repeatMode = state.repeatMode,
            isResolvingStream = true,
            isBuffering = false,
        )
        onTrackPlayed(track)
        resolveStream(track)
    }

    fun togglePlayPause() {
        val track = state.currentTrack ?: return
        val nextPlaying = !state.isPlaying
        if (nextPlaying) {
            if (audioEngine.hasMedia) {
                audioEngine.play()
            } else if (state.streamUrl == null) {
                resolveStream(track)
            }
        } else {
            if (audioEngine.hasMedia) {
                audioEngine.pause()
            }
        }
        state = state.copy(isPlaying = nextPlaying, playbackError = null)
    }

    fun next() {
        val nextIndex = state.queueIndex + 1
        when {
            nextIndex < state.queue.size -> moveTo(nextIndex)
            state.repeatMode == RepeatMode.All && state.queue.isNotEmpty() -> moveTo(0)
            else -> stopAtQueueEnd()
        }
    }

    fun previous() {
        if (state.positionMs > 3_000L) {
            audioEngine.seekTo(0L)
            state = state.copy(positionMs = 0L)
            return
        }
        val previousIndex = state.queueIndex - 1
        when {
            previousIndex >= 0 -> moveTo(previousIndex)
            state.repeatMode == RepeatMode.All && state.queue.isNotEmpty() -> moveTo(state.queue.lastIndex)
            else -> {
                audioEngine.seekTo(0L)
                state = state.copy(positionMs = 0L)
            }
        }
    }

    fun seekTo(positionMs: Long) {
        val bounded = positionMs.coerceIn(0L, state.durationMs)
        if (audioEngine.hasMedia) {
            audioEngine.seekTo(bounded)
        }
        state = state.copy(positionMs = bounded)
    }

    fun setShuffle(enabled: Boolean) {
        if (enabled == state.shuffleEnabled) return
        val oldQueue = state.queue
        val current = state.currentTrack
        state = if (enabled && oldQueue.size > 1 && current != null) {
            val rest = oldQueue.filterIndexed { i, _ -> i != state.queueIndex }.shuffled()
            state.copy(queue = listOf(current) + rest, queueIndex = 0)
        } else {
            state.copy(shuffleEnabled = enabled)
        }
    }

    fun cycleRepeatMode() {
        val nextMode = when (state.repeatMode) {
            RepeatMode.Off -> RepeatMode.All
            RepeatMode.All -> RepeatMode.One
            RepeatMode.One -> RepeatMode.Off
        }
        state = state.copy(repeatMode = nextMode)
    }

    fun release() {
        resolveJob?.cancel()
        tickJob?.cancel()
        tickJob = null
        audioEngine.release()
    }

    private fun moveTo(index: Int) {
        val queue = state.queue
        if (queue.isEmpty()) return
        val bounded = index.coerceIn(0, queue.lastIndex)
        val track = queue[bounded]
        audioEngine.stop()
        state = state.copy(
            currentTrack = track,
            positionMs = 0L,
            durationMs = track.durationMs,
            queueIndex = bounded,
            isPlaying = true,
            streamUrl = null,
            streamProvider = null,
            playbackError = null,
            isResolvingStream = true,
            isBuffering = false,
        )
        onTrackPlayed(track)
        resolveStream(track)
    }

    private fun stopAtQueueEnd() {
        audioEngine.pause()
        state = state.copy(
            isPlaying = false,
            positionMs = state.durationMs,
        )
    }

    private fun handleEnded() {
        when {
            state.repeatMode == RepeatMode.One -> {
                audioEngine.seekTo(0L)
                if (audioEngine.hasMedia) audioEngine.play()
                state = state.copy(positionMs = 0L, isPlaying = true)
            }

            state.queueIndex + 1 < state.queue.size -> moveTo(state.queueIndex + 1)
            state.repeatMode == RepeatMode.All && state.queue.isNotEmpty() -> moveTo(0)
            else -> state = state.copy(
                isPlaying = false,
                positionMs = state.durationMs,
            )
        }
    }

    /**
     * Resolves a short-lived stream URL through the backend, then hands it to Media3. The backend
     * owns the provider mapping; the client never talks to Audius directly. Fallback gracefully
     * to local simulation mode if offline or backend provider unmapped.
     */
    private fun resolveStream(track: Track) {
        resolveJob?.cancel()
        resolveJob = coroutineScope.launch {
            state = state.copy(isResolvingStream = true, playbackError = null)
            runCatching { apiClient.resolveStreamUrl(track.id.value) }
                .onSuccess { playback ->
                    if (state.currentTrack?.id != track.id) return@launch
                    state = state.copy(
                        streamUrl = playback.streamUrl,
                        streamProvider = playback.provider,
                        isResolvingStream = false,
                    )
                    audioEngine.load(playback.streamUrl, playWhenReady = state.isPlaying)
                }
                .onFailure { error ->
                    if (state.currentTrack?.id != track.id) return@launch
                    Log.i(TAG, "Backend stream unmapped or offline (${error.message}); using local engine mode.")
                    state = state.copy(
                        isBuffering = false,
                        isResolvingStream = false,
                        playbackError = null,
                    )
                }
        }
    }

    private fun startTicker() {
        if (tickJob?.isActive == true) return
        tickJob = coroutineScope.launch {
            while (isActive) {
                delay(msPerTick)
                val current = state
                if (current.isPlaying) {
                    if (audioEngine.hasMedia) {
                        val duration = audioEngine.durationMs.takeIf { it > 0L } ?: current.durationMs
                        state = current.copy(
                            positionMs = audioEngine.positionMs.coerceIn(0L, duration),
                            durationMs = duration,
                        )
                    } else {
                        // Fallback simulation playback ticker mode
                        val nextPos = current.positionMs + msPerTick
                        if (nextPos >= current.durationMs) {
                            handleEnded()
                        } else {
                            state = current.copy(positionMs = nextPos)
                        }
                    }
                }
            }
        }
    }

    private companion object {
        const val TAG = "PlayerController"
    }
}
