package com.stack.domain.usecase.playback

import javax.inject.Inject

class PlayTrackUseCase @Inject constructor() {
    suspend operator fun invoke(track: com.stack.domain.model.Track, queue: List<com.stack.domain.model.Track>, startIndex: Int) {
        TODO("Not yet implemented")
    }
}
