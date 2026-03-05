package com.stack.domain.usecase.library

import javax.inject.Inject

class DeleteTrackUseCase @Inject constructor() {
    suspend operator fun invoke(trackId: Long): Unit {
        TODO("Not yet implemented")
    }
}
