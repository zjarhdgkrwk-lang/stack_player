package com.stack.domain.usecase.playlist

import com.stack.domain.repository.PlaylistRepository
import javax.inject.Inject

class UpdatePlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: Long, name: String, description: String?) {
        val playlist = playlistRepository.getPlaylistById(playlistId) ?: return
        playlistRepository.updatePlaylist(
            playlist.copy(
                name = name,
                description = description,
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}
