package com.stack.domain.usecase.library

import javax.inject.Inject

class GetArtistDetailUseCase @Inject constructor() {
    suspend operator fun invoke(artistId: Long): com.stack.domain.model.Artist? {
        TODO("Not yet implemented")
    }
}
