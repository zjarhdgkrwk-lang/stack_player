package com.stack.domain.usecase.playlist

import com.stack.domain.model.Playlist
import com.stack.domain.repository.PlaylistRepository
import javax.inject.Inject

class CreatePlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(name: String, description: String? = null): Long {
        val now = System.currentTimeMillis()
        return playlistRepository.insertPlaylist(
            Playlist(
                id = 0,
                name = name,
                description = description,
                coverArtUri = null,
                trackCount = 0,
                totalDuration = 0,
                createdAt = now,
                updatedAt = now
            )
        )
    }
}
