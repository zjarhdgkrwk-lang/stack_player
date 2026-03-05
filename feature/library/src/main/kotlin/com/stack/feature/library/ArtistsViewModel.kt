package com.stack.feature.library

import androidx.lifecycle.viewModelScope
import com.stack.core.ui.BaseViewModel
import com.stack.domain.model.Artist
import com.stack.domain.model.enums.SortOrder
import com.stack.domain.repository.ArtistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArtistsState(
    val artists: List<Artist> = emptyList(),
    val sortOrder: SortOrder = SortOrder.TITLE_ASC,
    val isLoading: Boolean = true
)

sealed interface ArtistsIntent {
    data class SetSortOrder(val order: SortOrder) : ArtistsIntent
}

sealed interface ArtistsEffect

@HiltViewModel
class ArtistsViewModel @Inject constructor(
    private val artistRepository: ArtistRepository
) : BaseViewModel<ArtistsState, ArtistsIntent, ArtistsEffect>(ArtistsState()) {

    init {
        loadArtists()
    }

    override fun dispatch(intent: ArtistsIntent) {
        when (intent) {
            is ArtistsIntent.SetSortOrder -> {
                updateState { copy(sortOrder = intent.order) }
                loadArtists()
            }
        }
    }

    private fun loadArtists() {
        viewModelScope.launch {
            artistRepository.getAllArtists(currentState.sortOrder).collectLatest { artists ->
                updateState { copy(artists = artists, isLoading = false) }
            }
        }
    }
}
