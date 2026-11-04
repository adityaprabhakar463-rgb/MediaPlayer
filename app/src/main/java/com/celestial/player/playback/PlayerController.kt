package com.celestial.player.playback

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.celestial.player.domain.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlayerController(
    initMsPerTick: Long = 500L,
    private val onTrackPlayed: (Track) -> Unit = {},
    private val coroutineScope: CoroutineScope,
) {

    var state by mutableStateOf(PlayerUiState())
        private set

    private val msPerTick = initMsPerTick
    private var tickJob: Job? = null

    fun play(track: Track, queue: List<Track> = listOf(track)) {
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
        )
        onTrackPlayed(track)
        ensureTicking()
    }

    fun togglePlayPause() {
        if (state.currentTrack == null) return
        if (state.isPlaying) {
            stopTicking()
        } else {
            ensureTicking()
        }
        state = state.copy(isPlaying = !state.isPlaying)
    }

    fun next() {
        moveTo(index = state.queueIndex + 1)
    }

    fun previous() {
        if (state.positionMs > 3_000L) {
            state = state.copy(positionMs = 0L)
            return
        }
        moveTo(index = state.queueIndex - 1)
    }

    fun seekTo(positionMs: Long) {
        state = state.copy(positionMs = positionMs.coerceIn(0L, state.durationMs))
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

    private fun moveTo(index: Int) {
        val queue = state.queue
        if (queue.isEmpty()) {
            stopTicking()
            return
        }
        val bounded = when {
            index < 0 -> if (state.repeatMode == RepeatMode.All) queue.lastIndex else 0
            index >= queue.size -> if (state.repeatMode == RepeatMode.All) 0 else queue.lastIndex
            else -> index
        }
        val track = queue[bounded]
        state = state.copy(
            currentTrack = track,
            positionMs = 0L,
            durationMs = track.durationMs,
            queueIndex = bounded,
            isPlaying = true,
        )
        onTrackPlayed(track)
        ensureTicking()
    }

    private fun ensureTicking() {
        if (tickJob?.isActive == true) return
        tickJob = coroutineScope.launch {
            while (isActive) {
                delay(msPerTick)
                val s = state
                if (!s.isPlaying) return@launch
                val nextPos = s.positionMs + msPerTick
                if (nextPos >= s.durationMs) {
                    if (s.repeatMode == RepeatMode.One) {
                        state = s.copy(positionMs = 0L)
                    } else {
                        state = s.copy(positionMs = s.durationMs)
                        moveTo(index = s.queueIndex + 1)
                    }
                } else {
                    state = s.copy(positionMs = nextPos)
                }
            }
        }
    }

    private fun stopTicking() {
        tickJob?.cancel()
        tickJob = null
    }
}
