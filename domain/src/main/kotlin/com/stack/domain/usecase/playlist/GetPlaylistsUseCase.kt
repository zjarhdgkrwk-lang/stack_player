package com.stack.domain.usecase.playlist

import com.stack.domain.model.Playlist
import com.stack.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPlaylistsUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    operator fun invoke(): Flow<List<Playlist>> = playlistRepository.getAllPlaylists()
}
