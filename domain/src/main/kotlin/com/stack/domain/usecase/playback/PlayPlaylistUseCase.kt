package com.stack.domain.usecase.playback

import com.stack.domain.model.Track
import com.stack.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PlayPlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: Long, shuffled: Boolean = false): List<Track>? {
        val playlistTracks = playlistRepository.getPlaylistTracks(playlistId).first()
        if (playlistTracks.isEmpty()) return null
        val tracks = playlistTracks.map { it.track }
        return if (shuffled) tracks.shuffled() else tracks
    }
}
