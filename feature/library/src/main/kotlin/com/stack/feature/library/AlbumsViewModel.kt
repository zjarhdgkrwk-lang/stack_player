package com.stack.feature.library

import androidx.lifecycle.viewModelScope
import com.stack.core.ui.BaseViewModel
import com.stack.domain.model.Album
import com.stack.domain.model.enums.SortOrder
import com.stack.domain.repository.AlbumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlbumsState(
    val albums: List<Album> = emptyList(),
    val sortOrder: SortOrder = SortOrder.TITLE_ASC,
    val isLoading: Boolean = true
)

sealed interface AlbumsIntent {
    data class SetSortOrder(val order: SortOrder) : AlbumsIntent
}

sealed interface AlbumsEffect

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val albumRepository: AlbumRepository
) : BaseViewModel<AlbumsState, AlbumsIntent, AlbumsEffect>(AlbumsState()) {

    init {
        loadAlbums()
    }

    override fun dispatch(intent: AlbumsIntent) {
        when (intent) {
            is AlbumsIntent.SetSortOrder -> {
                updateState { copy(sortOrder = intent.order) }
                loadAlbums()
            }
        }
    }

    private fun loadAlbums() {
        viewModelScope.launch {
            albumRepository.getAllAlbums(currentState.sortOrder).collectLatest { albums ->
                updateState { copy(albums = albums, isLoading = false) }
            }
        }
    }
}
