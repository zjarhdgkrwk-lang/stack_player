package com.stack.domain.usecase.playback

import javax.inject.Inject

class PlayPlaylistUseCase @Inject constructor() {
    suspend operator fun invoke(playlistId: Long, shuffled: Boolean = false) {
        TODO("Not yet implemented")
    }
}
