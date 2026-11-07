package com.celestial.player.playback

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

/**
 * Thin Media3 boundary. [PlayerController] owns playback semantics; this class only handles the
 * audio pipeline and reports state changes. Keeping ExoPlayer behind this interface makes the
 * controller testable and prevents Compose code from touching a player instance directly.
 */
class AudioEngine(
    context: Context,
    private val listener: Listener,
) {
    interface Listener {
        fun onIsPlayingChanged(isPlaying: Boolean)
        fun onPlaybackStateChanged(state: Int)
        fun onError(message: String)
    }

    private val player: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            /* handleAudioFocus = */ true,
        )
        addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                listener.onIsPlayingChanged(isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                listener.onPlaybackStateChanged(playbackState)
            }

            override fun onPlayerError(error: PlaybackException) {
                listener.onError(error.message ?: "Playback failed")
            }
        })
    }

    var hasMedia: Boolean = false
        private set

    val isPlaying: Boolean get() = player.isPlaying

    val positionMs: Long
        get() = player.currentPosition.coerceAtLeast(0L)

    val durationMs: Long
        get() = player.duration.takeIf { it > 0L } ?: 0L

    fun load(url: String, playWhenReady: Boolean) {
        hasMedia = true
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        player.playWhenReady = playWhenReady
    }

    fun play() {
        if (hasMedia) player.play()
    }

    fun pause() {
        if (hasMedia) player.pause()
    }

    fun seekTo(positionMs: Long) {
        if (hasMedia) player.seekTo(positionMs.coerceAtLeast(0L))
    }

    fun stop() {
        player.stop()
        player.clearMediaItems()
        hasMedia = false
    }

    fun release() {
        player.release()
        hasMedia = false
    }
}
