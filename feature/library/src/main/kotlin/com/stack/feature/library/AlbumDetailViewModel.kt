package com.stack.feature.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.stack.core.ui.BaseViewModel
import com.stack.domain.model.Album
import com.stack.domain.model.Track
import com.stack.domain.repository.AlbumRepository
import com.stack.domain.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlbumDetailState(
    val album: Album? = null,
    val tracks: List<Track> = emptyList(),
    val isLoading: Boolean = true
)

sealed interface AlbumDetailIntent
sealed interface AlbumDetailEffect

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val albumRepository: AlbumRepository,
    private val trackRepository: TrackRepository
) : BaseViewModel<AlbumDetailState, AlbumDetailIntent, AlbumDetailEffect>(AlbumDetailState()) {

    private val albumId: Long = savedStateHandle.get<Long>("albumId") ?: -1L

    init {
        viewModelScope.launch {
            val album = albumRepository.getAlbumById(albumId)
            updateState { copy(album = album) }
        }
        viewModelScope.launch {
            trackRepository.getTracksByAlbumId(albumId).collectLatest { tracks ->
                updateState { copy(tracks = tracks, isLoading = false) }
            }
        }
    }

    override fun dispatch(intent: AlbumDetailIntent) {}
}
