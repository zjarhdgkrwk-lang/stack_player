package com.stack.feature.tags

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.stack.core.ui.BaseViewModel
import com.stack.domain.model.Track
import com.stack.domain.repository.TagRepository
import com.stack.domain.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TagDetailState(
    val tagName: String = "",
    val tracks: List<Track> = emptyList(),
    val isLoading: Boolean = true
)

sealed interface TagDetailIntent

sealed interface TagDetailEffect

@HiltViewModel
class TagDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tagRepository: TagRepository,
    private val trackRepository: TrackRepository
) : BaseViewModel<TagDetailState, TagDetailIntent, TagDetailEffect>(TagDetailState()) {

    private val tagId: Long = savedStateHandle.get<Long>("tagId") ?: -1L

    init {
        loadTagDetail()
    }

    override fun dispatch(intent: TagDetailIntent) {}

    private fun loadTagDetail() {
        viewModelScope.launch {
            val tag = tagRepository.getTagById(tagId)
            updateState { copy(tagName = tag?.name ?: "") }

            tagRepository.getTagsForTrack(tagId) // This returns tags for a track, not tracks for a tag
            // We need to use getTrackIdsForTag and load tracks
            val trackIds = tagRepository.getTrackIdsForTag(tagId)
            val tracks = trackIds.mapNotNull { trackRepository.getTrackById(it) }
            updateState { copy(tracks = tracks, isLoading = false) }
        }
    }
}
