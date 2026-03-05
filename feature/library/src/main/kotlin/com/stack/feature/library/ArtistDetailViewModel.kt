package com.stack.feature.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.stack.core.ui.BaseViewModel
import com.stack.domain.model.Artist
import com.stack.domain.model.Track
import com.stack.domain.repository.ArtistRepository
import com.stack.domain.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArtistDetailState(
    val artist: Artist? = null,
    val tracks: List<Track> = emptyList(),
    val isLoading: Boolean = true
)

sealed interface ArtistDetailIntent
sealed interface ArtistDetailEffect

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val artistRepository: ArtistRepository,
    private val trackRepository: TrackRepository
) : BaseViewModel<ArtistDetailState, ArtistDetailIntent, ArtistDetailEffect>(ArtistDetailState()) {

    private val artistId: Long = savedStateHandle.get<Long>("artistId") ?: -1L

    init {
        viewModelScope.launch {
            val artist = artistRepository.getArtistById(artistId)
            updateState { copy(artist = artist) }
        }
        viewModelScope.launch {
            trackRepository.getTracksByArtistId(artistId).collectLatest { tracks ->
                updateState { copy(tracks = tracks, isLoading = false) }
            }
        }
    }

    override fun dispatch(intent: ArtistDetailIntent) {}
}
