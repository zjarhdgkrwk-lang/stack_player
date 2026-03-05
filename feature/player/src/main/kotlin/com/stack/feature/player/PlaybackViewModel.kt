package com.stack.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stack.core.player.CommandDispatcher
import com.stack.core.player.PlaybackCommand
import com.stack.core.player.PlaybackState
import com.stack.core.player.PlaybackStateReducer
import com.stack.domain.model.Track
import com.stack.domain.model.enums.RepeatMode
import com.stack.domain.model.enums.ShuffleMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaybackViewModel @Inject constructor(
    private val commandDispatcher: CommandDispatcher,
    stateReducer: PlaybackStateReducer
) : ViewModel() {

    val playbackState: StateFlow<PlaybackState> = stateReducer.state

    fun play(track: Track, queue: List<Track>, startIndex: Int) {
        dispatch(PlaybackCommand.Play(track, queue, startIndex))
    }

    fun playAt(index: Int) {
        dispatch(PlaybackCommand.PlayAt(index))
    }

    fun pause() {
        dispatch(PlaybackCommand.Pause)
    }

    fun resume() {
        dispatch(PlaybackCommand.Resume)
    }

    fun togglePlayPause() {
        if (playbackState.value.isPlaying) pause() else resume()
    }

    fun skipNext() {
        dispatch(PlaybackCommand.SkipNext)
    }

    fun skipPrevious() {
        dispatch(PlaybackCommand.SkipPrevious)
    }

    fun seekTo(positionMs: Long) {
        dispatch(PlaybackCommand.SeekTo(positionMs))
    }

    fun addNext(track: Track) {
        dispatch(PlaybackCommand.AddNext(track))
    }

    fun addToQueue(track: Track) {
        dispatch(PlaybackCommand.AddToQueue(track))
    }

    fun removeFromQueue(index: Int) {
        dispatch(PlaybackCommand.RemoveFromQueue(index))
    }

    fun reorderQueue(from: Int, to: Int) {
        dispatch(PlaybackCommand.ReorderQueue(from, to))
    }

    fun setRepeatMode(mode: RepeatMode) {
        dispatch(PlaybackCommand.SetRepeatMode(mode))
    }

    fun toggleRepeatMode() {
        val current = playbackState.value.repeatMode
        val next = when (current) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        setRepeatMode(next)
    }

    fun setShuffleMode(mode: ShuffleMode) {
        dispatch(PlaybackCommand.SetShuffleMode(mode))
    }

    fun toggleShuffle() {
        val current = playbackState.value.shuffleMode
        val next = if (current == ShuffleMode.OFF) ShuffleMode.ON else ShuffleMode.OFF
        setShuffleMode(next)
    }

    fun setPlaybackSpeed(speed: Float) {
        dispatch(PlaybackCommand.SetPlaybackSpeed(speed))
    }

    fun setCrossfadeDuration(durationMs: Int) {
        dispatch(PlaybackCommand.SetCrossfadeDuration(durationMs))
    }

    fun setABRepeatA(positionMs: Long) {
        dispatch(PlaybackCommand.SetABRepeatA(positionMs))
    }

    fun setABRepeatB(positionMs: Long) {
        dispatch(PlaybackCommand.SetABRepeatB(positionMs))
    }

    fun clearABRepeat() {
        dispatch(PlaybackCommand.ClearABRepeat)
    }

    fun stop() {
        dispatch(PlaybackCommand.Stop)
    }

    private fun dispatch(command: PlaybackCommand) {
        viewModelScope.launch {
            commandDispatcher.dispatch(command)
        }
    }
}
