package com.stack.domain.usecase.library

import javax.inject.Inject

class GetFolderContentsUseCase @Inject constructor() {
    suspend operator fun invoke(relativePath: String): kotlinx.coroutines.flow.Flow<List<com.stack.domain.model.Track>> {
        TODO("Not yet implemented")
    }
}
