package com.stack.feature.playlists

import androidx.lifecycle.viewModelScope
import com.stack.core.ui.BaseViewModel
import com.stack.domain.model.Playlist
import com.stack.domain.usecase.playlist.CreatePlaylistUseCase
import com.stack.domain.usecase.playlist.DeletePlaylistUseCase
import com.stack.domain.usecase.playlist.GetPlaylistsUseCase
import com.stack.domain.usecase.playlist.UpdatePlaylistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaylistsState(
    val playlists: List<Playlist> = emptyList(),
    val isLoading: Boolean = true,
    val showEditor: Boolean = false,
    val editingPlaylist: Playlist? = null
)

sealed interface PlaylistsIntent {
    data object ShowCreateDialog : PlaylistsIntent
    data class ShowEditDialog(val playlist: Playlist) : PlaylistsIntent
    data object DismissDialog : PlaylistsIntent
    data class CreatePlaylist(val name: String, val description: String?) : PlaylistsIntent
    data class UpdatePlaylist(val id: Long, val name: String, val description: String?) : PlaylistsIntent
    data class DeletePlaylist(val id: Long) : PlaylistsIntent
}

sealed interface PlaylistsEffect

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val createPlaylistUseCase: CreatePlaylistUseCase,
    private val updatePlaylistUseCase: UpdatePlaylistUseCase,
    private val deletePlaylistUseCase: DeletePlaylistUseCase
) : BaseViewModel<PlaylistsState, PlaylistsIntent, PlaylistsEffect>(PlaylistsState()) {

    init {
        loadPlaylists()
    }

    override fun dispatch(intent: PlaylistsIntent) {
        when (intent) {
            PlaylistsIntent.ShowCreateDialog -> updateState { copy(showEditor = true, editingPlaylist = null) }
            is PlaylistsIntent.ShowEditDialog -> updateState { copy(showEditor = true, editingPlaylist = intent.playlist) }
            PlaylistsIntent.DismissDialog -> updateState { copy(showEditor = false, editingPlaylist = null) }
            is PlaylistsIntent.CreatePlaylist -> createPlaylist(intent.name, intent.description)
            is PlaylistsIntent.UpdatePlaylist -> updatePlaylist(intent.id, intent.name, intent.description)
            is PlaylistsIntent.DeletePlaylist -> deletePlaylist(intent.id)
        }
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            getPlaylistsUseCase().collectLatest { playlists ->
                updateState { copy(playlists = playlists, isLoading = false) }
            }
        }
    }

    private fun createPlaylist(name: String, description: String?) {
        viewModelScope.launch {
            createPlaylistUseCase(name, description)
            updateState { copy(showEditor = false, editingPlaylist = null) }
        }
    }

    private fun updatePlaylist(id: Long, name: String, description: String?) {
        viewModelScope.launch {
            updatePlaylistUseCase(id, name, description)
            updateState { copy(showEditor = false, editingPlaylist = null) }
        }
    }

    private fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            deletePlaylistUseCase(id)
        }
    }
}
