package com.stack.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stack.core.player.CommandDispatcher
import com.stack.core.player.PlaybackCommand
import com.stack.core.player.PlaybackState
import com.stack.core.player.PlaybackStateReducer
import com.stack.domain.model.Playlist
import com.stack.domain.model.Tag
import com.stack.domain.model.Track
import com.stack.domain.model.enums.RepeatMode
import com.stack.domain.model.enums.ShuffleMode
import com.stack.domain.model.enums.SystemTagType
import com.stack.domain.repository.PlaylistRepository
import com.stack.domain.repository.TagRepository
import com.stack.domain.usecase.playlist.AddTrackToPlaylistUseCase
import com.stack.domain.usecase.tag.AssignTagToTrackUseCase
import com.stack.domain.usecase.tag.RemoveTagFromTrackUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrackContextMenuState(
    val showPlaylistPicker: Boolean = false,
    val showTagAssigner: Boolean = false,
    val contextTrack: Track? = null,
    val playlists: List<Playlist> = emptyList(),
    val allTags: List<Tag> = emptyList(),
    val contextTrackTagIds: Set<Long> = emptySet()
)

@HiltViewModel
class PlaybackViewModel @Inject constructor(
    private val commandDispatcher: CommandDispatcher,
    stateReducer: PlaybackStateReducer,
    private val tagRepository: TagRepository,
    private val playlistRepository: PlaylistRepository,
    private val addTrackToPlaylistUseCase: AddTrackToPlaylistUseCase,
    private val assignTagToTrackUseCase: AssignTagToTrackUseCase,
    private val removeTagFromTrackUseCase: RemoveTagFromTrackUseCase
) : ViewModel() {

    val playbackState: StateFlow<PlaybackState> = stateReducer.state

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _contextMenuState = MutableStateFlow(TrackContextMenuState())
    val contextMenuState: StateFlow<TrackContextMenuState> = _contextMenuState.asStateFlow()

    init {
        observeCurrentTrackFavorite()
    }

    private fun observeCurrentTrackFavorite() {
        viewModelScope.launch {
            playbackState
                .map { it.currentTrack?.id }
                .distinctUntilChanged()
                .collectLatest { trackId ->
                    if (trackId != null) {
                        val favoriteTag = tagRepository.getTagBySystemType(SystemTagType.FAVORITE)
                        if (favoriteTag != null) {
                            _isFavorite.value = tagRepository.isTrackTagged(trackId, favoriteTag.id)
                        } else {
                            _isFavorite.value = false
                        }
                    } else {
                        _isFavorite.value = false
                    }
                }
        }
    }

    fun toggleFavorite() {
        val trackId = playbackState.value.currentTrack?.id ?: return
        viewModelScope.launch {
            val favoriteTag = tagRepository.getTagBySystemType(SystemTagType.FAVORITE) ?: return@launch
            val wasFavorite = tagRepository.isTrackTagged(trackId, favoriteTag.id)
            if (wasFavorite) {
                tagRepository.removeTagFromTrack(trackId, favoriteTag.id)
            } else {
                tagRepository.assignTagToTrack(trackId, favoriteTag.id)
            }
            _isFavorite.value = !wasFavorite
        }
    }

    fun toggleFavoriteForTrack(track: Track) {
        viewModelScope.launch {
            val favoriteTag = tagRepository.getTagBySystemType(SystemTagType.FAVORITE) ?: return@launch
            val wasFavorite = tagRepository.isTrackTagged(track.id, favoriteTag.id)
            if (wasFavorite) {
                tagRepository.removeTagFromTrack(track.id, favoriteTag.id)
            } else {
                tagRepository.assignTagToTrack(track.id, favoriteTag.id)
            }
            // If this is the current track, update isFavorite
            if (playbackState.value.currentTrack?.id == track.id) {
                _isFavorite.value = !wasFavorite
            }
        }
    }

    // Context menu: Add to Playlist
    fun showPlaylistPicker(track: Track) {
        viewModelScope.launch {
            val playlists = playlistRepository.getAllPlaylists().first()
            _contextMenuState.value = _contextMenuState.value.copy(
                showPlaylistPicker = true,
                contextTrack = track,
                playlists = playlists
            )
        }
    }

    fun dismissPlaylistPicker() {
        _contextMenuState.value = _contextMenuState.value.copy(
            showPlaylistPicker = false,
            contextTrack = null
        )
    }

    fun addTrackToPlaylist(playlistId: Long) {
        val trackId = _contextMenuState.value.contextTrack?.id ?: return
        viewModelScope.launch {
            addTrackToPlaylistUseCase(playlistId, trackId)
            _contextMenuState.value = _contextMenuState.value.copy(
                showPlaylistPicker = false,
                contextTrack = null
            )
        }
    }

    // Context menu: Assign Tag
    fun showTagAssigner(track: Track) {
        viewModelScope.launch {
            val allTags = tagRepository.getAllTags().first()
            val trackTags = tagRepository.getTagsForTrack(track.id).first()
            _contextMenuState.value = _contextMenuState.value.copy(
                showTagAssigner = true,
                contextTrack = track,
                allTags = allTags,
                contextTrackTagIds = trackTags.map { it.id }.toSet()
            )
        }
    }

    fun dismissTagAssigner() {
        _contextMenuState.value = _contextMenuState.value.copy(
            showTagAssigner = false,
            contextTrack = null,
            contextTrackTagIds = emptySet()
        )
    }

    fun toggleTrackTag(tagId: Long) {
        val trackId = _contextMenuState.value.contextTrack?.id ?: return
        viewModelScope.launch {
            if (tagId in _contextMenuState.value.contextTrackTagIds) {
                removeTagFromTrackUseCase(trackId, tagId)
                _contextMenuState.value = _contextMenuState.value.copy(
                    contextTrackTagIds = _contextMenuState.value.contextTrackTagIds - tagId
                )
            } else {
                assignTagToTrackUseCase(trackId, tagId)
                _contextMenuState.value = _contextMenuState.value.copy(
                    contextTrackTagIds = _contextMenuState.value.contextTrackTagIds + tagId
                )
            }
        }
    }

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
