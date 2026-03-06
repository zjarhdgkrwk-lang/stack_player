package com.stack.domain.usecase.playlist

import com.stack.domain.repository.PlaylistRepository
import javax.inject.Inject

class RemoveTrackFromPlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: Long, trackId: Long) {
        playlistRepository.removeTrackFromPlaylist(playlistId, trackId)
    }
}
