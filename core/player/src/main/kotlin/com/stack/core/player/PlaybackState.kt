package com.stack.core.player

import com.stack.domain.model.Track
import com.stack.domain.model.enums.RepeatMode
import com.stack.domain.model.enums.ShuffleMode

data class PlaybackState(
    val currentTrack: Track? = null,
    val queue: List<Track> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleMode: ShuffleMode = ShuffleMode.OFF,
    val playbackSpeed: Float = 1.0f,
    val crossfadeDurationMs: Int = 0,
    val abRepeatA: Long? = null,
    val abRepeatB: Long? = null,
    val isBuffering: Boolean = false,
    val error: PlaybackError? = null
)
