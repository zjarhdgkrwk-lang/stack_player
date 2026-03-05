package com.stack.core.player

import com.stack.domain.model.Track
import com.stack.domain.model.enums.ShuffleMode

class PlaybackQueue {

    private var _original: MutableList<Track> = mutableListOf()
    private var _shuffled: MutableList<Track> = mutableListOf()
    private var _shuffleMode: ShuffleMode = ShuffleMode.OFF
    private var _currentIndex: Int = -1

    val currentIndex: Int get() = _currentIndex
    val currentTrack: Track? get() = activeList.getOrNull(_currentIndex)
    val tracks: List<Track> get() = activeList.toList()
    val size: Int get() = activeList.size
    val isEmpty: Boolean get() = activeList.isEmpty()

    private val activeList: List<Track>
        get() = if (_shuffleMode == ShuffleMode.ON) _shuffled else _original

    fun setQueue(tracks: List<Track>, startIndex: Int) {
        _original = tracks.toMutableList()
        _currentIndex = startIndex
        if (_shuffleMode == ShuffleMode.ON) {
            buildShuffledList()
        }
    }

    fun playAt(index: Int): Track? {
        if (index < 0 || index >= activeList.size) return null
        _currentIndex = index
        return currentTrack
    }

    fun skipNext(repeatMode: com.stack.domain.model.enums.RepeatMode): Track? {
        if (activeList.isEmpty()) return null

        return when (repeatMode) {
            com.stack.domain.model.enums.RepeatMode.ONE -> currentTrack
            com.stack.domain.model.enums.RepeatMode.ALL -> {
                _currentIndex = (_currentIndex + 1) % activeList.size
                currentTrack
            }
            com.stack.domain.model.enums.RepeatMode.OFF -> {
                if (_currentIndex + 1 >= activeList.size) null
                else {
                    _currentIndex++
                    currentTrack
                }
            }
        }
    }

    fun skipPrevious(): Track? {
        if (activeList.isEmpty()) return null
        if (_currentIndex > 0) {
            _currentIndex--
        }
        return currentTrack
    }

    fun addNext(track: Track) {
        val insertIndex = (_currentIndex + 1).coerceAtMost(activeList.size)
        if (_shuffleMode == ShuffleMode.ON) {
            _shuffled.add(insertIndex, track)
            _original.add(track)
        } else {
            _original.add(insertIndex, track)
        }
    }

    fun addToQueue(track: Track) {
        if (_shuffleMode == ShuffleMode.ON) {
            _shuffled.add(track)
            _original.add(track)
        } else {
            _original.add(track)
        }
    }

    fun removeAt(index: Int) {
        if (index < 0 || index >= activeList.size) return
        val track = activeList[index]
        if (_shuffleMode == ShuffleMode.ON) {
            _shuffled.removeAt(index)
            _original.remove(track)
        } else {
            _original.removeAt(index)
        }
        if (index < _currentIndex) {
            _currentIndex--
        } else if (index == _currentIndex) {
            _currentIndex = _currentIndex.coerceAtMost(activeList.size - 1)
        }
    }

    fun reorder(from: Int, to: Int) {
        if (from < 0 || to < 0) return
        val list = if (_shuffleMode == ShuffleMode.ON) _shuffled else _original
        if (from >= list.size || to >= list.size) return
        val item = list.removeAt(from)
        list.add(to, item)
        // Adjust current index
        _currentIndex = when {
            from == _currentIndex -> to
            from < _currentIndex && to >= _currentIndex -> _currentIndex - 1
            from > _currentIndex && to <= _currentIndex -> _currentIndex + 1
            else -> _currentIndex
        }
    }

    fun setShuffle(mode: ShuffleMode) {
        val previousMode = _shuffleMode
        _shuffleMode = mode
        val currentTrackBefore = currentTrack

        if (mode == ShuffleMode.ON && previousMode == ShuffleMode.OFF) {
            buildShuffledList()
        } else if (mode == ShuffleMode.OFF && previousMode == ShuffleMode.ON) {
            // Return to original list, keep current track
            if (currentTrackBefore != null) {
                _currentIndex = _original.indexOf(currentTrackBefore).coerceAtLeast(0)
            }
        }
    }

    fun clear() {
        _original.clear()
        _shuffled.clear()
        _currentIndex = -1
    }

    private fun buildShuffledList() {
        val current = currentTrack
        _shuffled = _original.toMutableList()
        // Fisher-Yates shuffle, keeping current track at current position
        val n = _shuffled.size
        for (i in n - 1 downTo 1) {
            val j = (0..i).random()
            val temp = _shuffled[i]
            _shuffled[i] = _shuffled[j]
            _shuffled[j] = temp
        }
        // Move current track to currentIndex position
        if (current != null && _currentIndex >= 0 && _currentIndex < _shuffled.size) {
            val currentPos = _shuffled.indexOf(current)
            if (currentPos >= 0 && currentPos != _currentIndex) {
                _shuffled.removeAt(currentPos)
                _shuffled.add(_currentIndex, current)
            }
        }
    }
}
