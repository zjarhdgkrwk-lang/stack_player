package com.stack.core.player

import com.stack.domain.model.Track
import com.stack.domain.model.enums.RepeatMode
import com.stack.domain.model.enums.ShuffleMode

sealed interface PlaybackCommand {
    data class Play(val track: Track, val queue: List<Track>, val startIndex: Int) : PlaybackCommand
    data class PlayAt(val index: Int) : PlaybackCommand
    data object Pause : PlaybackCommand
    data object Resume : PlaybackCommand
    data object SkipNext : PlaybackCommand
    data object SkipPrevious : PlaybackCommand
    data class SeekTo(val positionMs: Long) : PlaybackCommand
    data class AddNext(val track: Track) : PlaybackCommand
    data class AddToQueue(val track: Track) : PlaybackCommand
    data class RemoveFromQueue(val index: Int) : PlaybackCommand
    data class ReorderQueue(val from: Int, val to: Int) : PlaybackCommand
    data class SetRepeatMode(val mode: RepeatMode) : PlaybackCommand
    data class SetShuffleMode(val mode: ShuffleMode) : PlaybackCommand
    data class SetPlaybackSpeed(val speed: Float) : PlaybackCommand
    data class SetCrossfadeDuration(val durationMs: Int) : PlaybackCommand
    data class SetABRepeatA(val positionMs: Long) : PlaybackCommand
    data class SetABRepeatB(val positionMs: Long) : PlaybackCommand
    data object ClearABRepeat : PlaybackCommand
    data object Stop : PlaybackCommand

    // Internal commands (from player callbacks)
    data class OnTrackEnded(val endedIndex: Int) : PlaybackCommand
    data class OnError(val error: PlaybackError) : PlaybackCommand
}
