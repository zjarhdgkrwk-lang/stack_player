package com.stack.domain.usecase.playlist

import com.stack.domain.repository.PlaylistRepository
import javax.inject.Inject

class AddTrackToPlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: Long, trackId: Long) {
        val maxIndex = playlistRepository.getMaxOrderIndex(playlistId)
        playlistRepository.addTrackToPlaylist(playlistId, trackId, maxIndex + 1)
    }
}
