package com.stack.feature.library

import com.stack.core.ui.BaseViewModel
import com.stack.domain.model.Track
import com.stack.domain.model.enums.SortOrder
import com.stack.domain.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class TracksState(
    val tracks: List<Track> = emptyList(),
    val sortOrder: SortOrder = SortOrder.DATE_ADDED_DESC,
    val isLoading: Boolean = true
)

sealed interface TracksIntent {
    data class SetSortOrder(val order: SortOrder) : TracksIntent
    data object Refresh : TracksIntent
}

sealed interface TracksEffect

@HiltViewModel
class TracksViewModel @Inject constructor(
    private val trackRepository: TrackRepository
) : BaseViewModel<TracksState, TracksIntent, TracksEffect>(TracksState()) {

    init {
        loadTracks()
    }

    override fun dispatch(intent: TracksIntent) {
        when (intent) {
            is TracksIntent.SetSortOrder -> {
                updateState { copy(sortOrder = intent.order) }
                loadTracks()
            }
            TracksIntent.Refresh -> loadTracks()
        }
    }

    private fun loadTracks() {
        viewModelScope.launch {
            trackRepository.getAllTracks(currentState.sortOrder).collectLatest { tracks ->
                updateState { copy(tracks = tracks, isLoading = false) }
            }
        }
    }
}
