package com.stack.feature.playlists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.stack.core.ui.BaseViewModel
import com.stack.domain.model.Playlist
import com.stack.domain.model.PlaylistTrack
import com.stack.domain.model.Track
import com.stack.domain.usecase.playlist.AddTrackToPlaylistUseCase
import com.stack.domain.usecase.playlist.GetPlaylistDetailUseCase
import com.stack.domain.usecase.playlist.RemoveTrackFromPlaylistUseCase
import com.stack.domain.usecase.playlist.ReorderPlaylistUseCase
import com.stack.domain.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaylistDetailState(
    val playlist: Playlist? = null,
    val playlistTracks: List<PlaylistTrack> = emptyList(),
    val isLoading: Boolean = true,
    val showTrackPicker: Boolean = false,
    val allTracks: List<Track> = emptyList()
)

sealed interface PlaylistDetailIntent {
    data class RemoveTrack(val trackId: Long) : PlaylistDetailIntent
    data class Reorder(val from: Int, val to: Int) : PlaylistDetailIntent
    data object ShowTrackPicker : PlaylistDetailIntent
    data object DismissTrackPicker : PlaylistDetailIntent
    data class AddTrack(val trackId: Long) : PlaylistDetailIntent
}

sealed interface PlaylistDetailEffect

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPlaylistDetailUseCase: GetPlaylistDetailUseCase,
    private val removeTrackFromPlaylistUseCase: RemoveTrackFromPlaylistUseCase,
    private val reorderPlaylistUseCase: ReorderPlaylistUseCase,
    private val addTrackToPlaylistUseCase: AddTrackToPlaylistUseCase,
    private val trackRepository: TrackRepository
) : BaseViewModel<PlaylistDetailState, PlaylistDetailIntent, PlaylistDetailEffect>(PlaylistDetailState()) {

    private val playlistId: Long = savedStateHandle.get<Long>("playlistId") ?: -1L

    init {
        loadPlaylistDetail()
    }

    override fun dispatch(intent: PlaylistDetailIntent) {
        when (intent) {
            is PlaylistDetailIntent.RemoveTrack -> removeTrack(intent.trackId)
            is PlaylistDetailIntent.Reorder -> reorder(intent.from, intent.to)
            PlaylistDetailIntent.ShowTrackPicker -> showTrackPicker()
            PlaylistDetailIntent.DismissTrackPicker -> updateState { copy(showTrackPicker = false) }
            is PlaylistDetailIntent.AddTrack -> addTrack(intent.trackId)
        }
    }

    private fun loadPlaylistDetail() {
        viewModelScope.launch {
            val playlist = getPlaylistDetailUseCase.getPlaylist(playlistId)
            updateState { copy(playlist = playlist) }

            getPlaylistDetailUseCase.getTracks(playlistId).collectLatest { tracks ->
                updateState { copy(playlistTracks = tracks, isLoading = false) }
            }
        }
    }

    private fun removeTrack(trackId: Long) {
        viewModelScope.launch {
            removeTrackFromPlaylistUseCase(playlistId, trackId)
        }
    }

    private fun reorder(from: Int, to: Int) {
        viewModelScope.launch {
            reorderPlaylistUseCase(playlistId, from, to)
        }
    }

    private fun showTrackPicker() {
        viewModelScope.launch {
            val allTracks = trackRepository.getAllTracks().first()
            updateState { copy(showTrackPicker = true, allTracks = allTracks) }
        }
    }

    private fun addTrack(trackId: Long) {
        viewModelScope.launch {
            addTrackToPlaylistUseCase(playlistId, trackId)
        }
    }
}
