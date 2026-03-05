package com.stack.core.player

import com.stack.core.logging.Logger
import com.stack.core.util.CoroutineDispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommandDispatcher @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
    private val stateReducer: PlaybackStateReducer,
    private val playerManager: StackPlayerManager,
    private val queue: PlaybackQueue
) {
    companion object {
        private const val TAG = "CommandDispatcher"
    }

    private val mutex = Mutex()

    suspend fun dispatch(command: PlaybackCommand) {
        mutex.withLock {
            withContext(dispatchers.main) {
                val snapshot = stateReducer.currentState
                Logger.d(TAG, "Dispatching: ${command::class.simpleName}")
                executeCommand(command, snapshot)
            }
        }
    }

    private suspend fun executeCommand(command: PlaybackCommand, snapshot: PlaybackState) {
        when (command) {
            is PlaybackCommand.Play -> handlePlay(command)
            is PlaybackCommand.PlayAt -> handlePlayAt(command)
            PlaybackCommand.Pause -> handlePause()
            PlaybackCommand.Resume -> handleResume()
            PlaybackCommand.SkipNext -> handleSkipNext(snapshot)
            PlaybackCommand.SkipPrevious -> handleSkipPrevious(snapshot)
            is PlaybackCommand.SeekTo -> handleSeekTo(command)
            is PlaybackCommand.AddNext -> handleAddNext(command)
            is PlaybackCommand.AddToQueue -> handleAddToQueue(command)
            is PlaybackCommand.RemoveFromQueue -> handleRemoveFromQueue(command)
            is PlaybackCommand.ReorderQueue -> handleReorderQueue(command)
            is PlaybackCommand.SetRepeatMode -> handleSetRepeatMode(command)
            is PlaybackCommand.SetShuffleMode -> handleSetShuffleMode(command)
            is PlaybackCommand.SetPlaybackSpeed -> handleSetPlaybackSpeed(command)
            is PlaybackCommand.SetCrossfadeDuration -> handleSetCrossfadeDuration(command)
            is PlaybackCommand.SetABRepeatA -> handleSetABRepeatA(command)
            is PlaybackCommand.SetABRepeatB -> handleSetABRepeatB(command)
            PlaybackCommand.ClearABRepeat -> handleClearABRepeat()
            PlaybackCommand.Stop -> handleStop()
            is PlaybackCommand.OnTrackEnded -> handleTrackEnded(command, snapshot)
            is PlaybackCommand.OnError -> handleError(command)
        }
    }

    private suspend fun handlePlay(command: PlaybackCommand.Play) {
        queue.setQueue(command.queue, command.startIndex)
        val track = command.track
        playerManager.play(track)
        val queueTracks = queue.tracks
        val queueIndex = queue.currentIndex
        stateReducer.updateState {
            copy(
                currentTrack = track,
                queue = queueTracks,
                currentIndex = queueIndex,
                isPlaying = true,
                positionMs = 0,
                durationMs = track.duration,
                error = null,
                abRepeatA = null,
                abRepeatB = null
            )
        }
    }

    private suspend fun handlePlayAt(command: PlaybackCommand.PlayAt) {
        val track = queue.playAt(command.index) ?: return
        playerManager.play(track)
        val queueIndex = queue.currentIndex
        stateReducer.updateState {
            copy(
                currentTrack = track,
                currentIndex = queueIndex,
                isPlaying = true,
                positionMs = 0,
                durationMs = track.duration,
                error = null,
                abRepeatA = null,
                abRepeatB = null
            )
        }
    }

    private fun handlePause() {
        playerManager.pause()
        stateReducer.updateState { copy(isPlaying = false) }
    }

    private fun handleResume() {
        playerManager.resume()
        stateReducer.updateState { copy(isPlaying = true) }
    }

    private suspend fun handleSkipNext(snapshot: PlaybackState) {
        val nextTrack = queue.skipNext(snapshot.repeatMode) ?: run {
            handleStop()
            return
        }
        playerManager.play(nextTrack)
        val queueTracks = queue.tracks
        val queueIndex = queue.currentIndex
        stateReducer.updateState {
            copy(
                currentTrack = nextTrack,
                queue = queueTracks,
                currentIndex = queueIndex,
                isPlaying = true,
                positionMs = 0,
                durationMs = nextTrack.duration,
                error = null,
                abRepeatA = null,
                abRepeatB = null
            )
        }
    }

    private suspend fun handleSkipPrevious(snapshot: PlaybackState) {
        // 3-second rule: if > 3s into track, restart; otherwise go previous
        if (snapshot.positionMs > 3000) {
            playerManager.seekTo(0)
            stateReducer.updateState { copy(positionMs = 0) }
        } else {
            val prevTrack = queue.skipPrevious() ?: return
            playerManager.play(prevTrack)
            val queueIndex = queue.currentIndex
            stateReducer.updateState {
                copy(
                    currentTrack = prevTrack,
                    currentIndex = queueIndex,
                    isPlaying = true,
                    positionMs = 0,
                    durationMs = prevTrack.duration,
                    error = null,
                    abRepeatA = null,
                    abRepeatB = null
                )
            }
        }
    }

    private fun handleSeekTo(command: PlaybackCommand.SeekTo) {
        playerManager.seekTo(command.positionMs)
        stateReducer.updateState { copy(positionMs = command.positionMs) }
    }

    private fun handleAddNext(command: PlaybackCommand.AddNext) {
        queue.addNext(command.track)
        val queueTracks = queue.tracks
        stateReducer.updateState { copy(queue = queueTracks) }
    }

    private fun handleAddToQueue(command: PlaybackCommand.AddToQueue) {
        queue.addToQueue(command.track)
        val queueTracks = queue.tracks
        stateReducer.updateState { copy(queue = queueTracks) }
    }

    private fun handleRemoveFromQueue(command: PlaybackCommand.RemoveFromQueue) {
        queue.removeAt(command.index)
        val queueTracks = queue.tracks
        val queueIndex = queue.currentIndex
        val queueCurrentTrack = queue.currentTrack
        stateReducer.updateState {
            copy(
                queue = queueTracks,
                currentIndex = queueIndex,
                currentTrack = queueCurrentTrack
            )
        }
    }

    private fun handleReorderQueue(command: PlaybackCommand.ReorderQueue) {
        queue.reorder(command.from, command.to)
        val queueTracks = queue.tracks
        val queueIndex = queue.currentIndex
        stateReducer.updateState {
            copy(
                queue = queueTracks,
                currentIndex = queueIndex
            )
        }
    }

    private fun handleSetRepeatMode(command: PlaybackCommand.SetRepeatMode) {
        stateReducer.updateState { copy(repeatMode = command.mode) }
    }

    private fun handleSetShuffleMode(command: PlaybackCommand.SetShuffleMode) {
        queue.setShuffle(command.mode)
        val queueTracks = queue.tracks
        val queueIndex = queue.currentIndex
        stateReducer.updateState {
            copy(
                shuffleMode = command.mode,
                queue = queueTracks,
                currentIndex = queueIndex
            )
        }
    }

    private fun handleSetPlaybackSpeed(command: PlaybackCommand.SetPlaybackSpeed) {
        playerManager.setPlaybackSpeed(command.speed)
        stateReducer.updateState { copy(playbackSpeed = command.speed) }
    }

    private fun handleSetCrossfadeDuration(command: PlaybackCommand.SetCrossfadeDuration) {
        stateReducer.updateState { copy(crossfadeDurationMs = command.durationMs) }
    }

    private fun handleSetABRepeatA(command: PlaybackCommand.SetABRepeatA) {
        stateReducer.updateState { copy(abRepeatA = command.positionMs) }
    }

    private fun handleSetABRepeatB(command: PlaybackCommand.SetABRepeatB) {
        stateReducer.updateState { copy(abRepeatB = command.positionMs) }
    }

    private fun handleClearABRepeat() {
        stateReducer.updateState { copy(abRepeatA = null, abRepeatB = null) }
    }

    private fun handleStop() {
        playerManager.stop()
        queue.clear()
        stateReducer.reset()
    }

    private suspend fun handleTrackEnded(command: PlaybackCommand.OnTrackEnded, snapshot: PlaybackState) {
        handleSkipNext(snapshot)
    }

    private fun handleError(command: PlaybackCommand.OnError) {
        stateReducer.updateState {
            copy(
                isPlaying = false,
                error = command.error
            )
        }
    }
}
