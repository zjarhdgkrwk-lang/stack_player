package com.stack.feature.library

import com.stack.core.ui.BaseViewModel
import com.stack.domain.model.Playlist
import com.stack.domain.model.Tag
import com.stack.domain.model.Track
import com.stack.domain.model.enums.SortOrder
import com.stack.domain.model.enums.SystemTagType
import com.stack.domain.repository.PlaylistRepository
import com.stack.domain.repository.TagRepository
import com.stack.domain.repository.TrackRepository
import com.stack.domain.usecase.playlist.AddTrackToPlaylistUseCase
import com.stack.domain.usecase.tag.AssignTagToTrackUseCase
import com.stack.domain.usecase.tag.RemoveTagFromTrackUseCase
import com.stack.domain.usecase.tag.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class TracksState(
    val tracks: List<Track> = emptyList(),
    val filteredTracks: List<Track> = emptyList(),
    val sortOrder: SortOrder = SortOrder.DATE_ADDED_DESC,
    val isLoading: Boolean = true,
    val allTags: List<Tag> = emptyList(),
    val selectedTagIds: Set<Long> = emptySet(),
    val tagFilterMode: TagFilterMode = TagFilterMode.AND,
    val showPlaylistPicker: Boolean = false,
    val showTagAssigner: Boolean = false,
    val contextTrack: Track? = null,
    val playlists: List<Playlist> = emptyList(),
    val contextTrackTagIds: Set<Long> = emptySet()
)

enum class TagFilterMode { AND, OR }

sealed interface TracksIntent {
    data class SetSortOrder(val order: SortOrder) : TracksIntent
    data object Refresh : TracksIntent
    data class ToggleTagFilter(val tagId: Long) : TracksIntent
    data class SetTagFilterMode(val mode: TagFilterMode) : TracksIntent
    data object ClearTagFilter : TracksIntent
    data class ShowPlaylistPicker(val track: Track) : TracksIntent
    data object DismissPlaylistPicker : TracksIntent
    data class AddToPlaylist(val playlistId: Long) : TracksIntent
    data class ShowTagAssigner(val track: Track) : TracksIntent
    data object DismissTagAssigner : TracksIntent
    data class ToggleTrackTag(val tagId: Long) : TracksIntent
    data class ToggleFavorite(val track: Track) : TracksIntent
}

sealed interface TracksEffect

@HiltViewModel
class TracksViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val tagRepository: TagRepository,
    private val playlistRepository: PlaylistRepository,
    private val addTrackToPlaylistUseCase: AddTrackToPlaylistUseCase,
    private val assignTagToTrackUseCase: AssignTagToTrackUseCase,
    private val removeTagFromTrackUseCase: RemoveTagFromTrackUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : BaseViewModel<TracksState, TracksIntent, TracksEffect>(TracksState()) {

    init {
        loadTracks()
        loadTags()
        loadPlaylists()
    }

    override fun dispatch(intent: TracksIntent) {
        when (intent) {
            is TracksIntent.SetSortOrder -> {
                updateState { copy(sortOrder = intent.order) }
                loadTracks()
            }
            TracksIntent.Refresh -> loadTracks()
            is TracksIntent.ToggleTagFilter -> {
                val current = currentState.selectedTagIds.toMutableSet()
                if (current.contains(intent.tagId)) {
                    current.remove(intent.tagId)
                } else {
                    current.add(intent.tagId)
                }
                updateState { copy(selectedTagIds = current) }
                applyTagFilter()
            }
            is TracksIntent.SetTagFilterMode -> {
                updateState { copy(tagFilterMode = intent.mode) }
                applyTagFilter()
            }
            TracksIntent.ClearTagFilter -> {
                updateState { copy(selectedTagIds = emptySet(), filteredTracks = currentState.tracks) }
            }
            is TracksIntent.ShowPlaylistPicker -> {
                updateState { copy(showPlaylistPicker = true, contextTrack = intent.track) }
            }
            TracksIntent.DismissPlaylistPicker -> {
                updateState { copy(showPlaylistPicker = false, contextTrack = null) }
            }
            is TracksIntent.AddToPlaylist -> addToPlaylist(intent.playlistId)
            is TracksIntent.ShowTagAssigner -> showTagAssigner(intent.track)
            TracksIntent.DismissTagAssigner -> {
                updateState { copy(showTagAssigner = false, contextTrack = null, contextTrackTagIds = emptySet()) }
            }
            is TracksIntent.ToggleTrackTag -> toggleTrackTag(intent.tagId)
            is TracksIntent.ToggleFavorite -> toggleFavorite(intent.track)
        }
    }

    private fun loadTracks() {
        viewModelScope.launch {
            trackRepository.getAllTracks(currentState.sortOrder).collectLatest { tracks ->
                updateState { copy(tracks = tracks, filteredTracks = tracks, isLoading = false) }
                if (currentState.selectedTagIds.isNotEmpty()) {
                    applyTagFilter()
                }
            }
        }
    }

    private fun loadTags() {
        viewModelScope.launch {
            tagRepository.getAllTags().collectLatest { tags ->
                updateState { copy(allTags = tags) }
            }
        }
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            playlistRepository.getAllPlaylists().collectLatest { playlists ->
                updateState { copy(playlists = playlists) }
            }
        }
    }

    private fun addToPlaylist(playlistId: Long) {
        val trackId = currentState.contextTrack?.id ?: return
        viewModelScope.launch {
            addTrackToPlaylistUseCase(playlistId, trackId)
            updateState { copy(showPlaylistPicker = false, contextTrack = null) }
        }
    }

    private fun showTagAssigner(track: Track) {
        viewModelScope.launch {
            val tags = tagRepository.getTagsForTrack(track.id).first()
            updateState {
                copy(
                    showTagAssigner = true,
                    contextTrack = track,
                    contextTrackTagIds = tags.map { it.id }.toSet()
                )
            }
        }
    }

    private fun toggleTrackTag(tagId: Long) {
        val trackId = currentState.contextTrack?.id ?: return
        viewModelScope.launch {
            if (tagId in currentState.contextTrackTagIds) {
                removeTagFromTrackUseCase(trackId, tagId)
                updateState { copy(contextTrackTagIds = contextTrackTagIds - tagId) }
            } else {
                assignTagToTrackUseCase(trackId, tagId)
                updateState { copy(contextTrackTagIds = contextTrackTagIds + tagId) }
            }
        }
    }

    private fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            toggleFavoriteUseCase(track.id)
        }
    }

    private fun applyTagFilter() {
        if (currentState.selectedTagIds.isEmpty()) {
            updateState { copy(filteredTracks = currentState.tracks) }
            return
        }

        viewModelScope.launch {
            val selectedTagIds = currentState.selectedTagIds
            val trackIdSets = selectedTagIds.map { tagId ->
                tagRepository.getTrackIdsForTag(tagId).toSet()
            }

            val matchingTrackIds = if (currentState.tagFilterMode == TagFilterMode.AND) {
                trackIdSets.reduceOrNull { acc, set -> acc.intersect(set) } ?: emptySet()
            } else {
                trackIdSets.reduceOrNull { acc, set -> acc.union(set) } ?: emptySet()
            }

            val filtered = currentState.tracks.filter { it.id in matchingTrackIds }
            updateState { copy(filteredTracks = filtered) }
        }
    }
}
