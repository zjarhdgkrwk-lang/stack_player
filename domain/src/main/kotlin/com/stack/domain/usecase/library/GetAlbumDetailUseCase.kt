package com.stack.domain.usecase.library

import javax.inject.Inject

class GetAlbumDetailUseCase @Inject constructor() {
    suspend operator fun invoke(albumId: Long): com.stack.domain.model.Album? {
        TODO("Not yet implemented")
    }
}
