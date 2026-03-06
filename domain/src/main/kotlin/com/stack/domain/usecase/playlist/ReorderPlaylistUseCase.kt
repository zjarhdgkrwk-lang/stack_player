package com.stack.domain.usecase.playlist

import com.stack.domain.repository.PlaylistRepository
import javax.inject.Inject

class ReorderPlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: Long, fromIndex: Int, toIndex: Int) {
        playlistRepository.reorderPlaylistTrack(playlistId, fromIndex, toIndex)
    }
}
