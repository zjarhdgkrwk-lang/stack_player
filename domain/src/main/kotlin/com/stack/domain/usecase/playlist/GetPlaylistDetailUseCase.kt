package com.stack.domain.usecase.playlist

import com.stack.domain.model.Playlist
import com.stack.domain.model.PlaylistTrack
import com.stack.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPlaylistDetailUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend fun getPlaylist(playlistId: Long): Playlist? =
        playlistRepository.getPlaylistById(playlistId)

    fun getTracks(playlistId: Long): Flow<List<PlaylistTrack>> =
        playlistRepository.getPlaylistTracks(playlistId)
}
