package com.stack.domain.usecase.library

import javax.inject.Inject

class GetArtistsUseCase @Inject constructor() {
    suspend operator fun invoke(sortOrder: com.stack.domain.model.enums.SortOrder = com.stack.domain.model.enums.SortOrder.TITLE_ASC): kotlinx.coroutines.flow.Flow<List<com.stack.domain.model.Artist>> {
        TODO("Not yet implemented")
    }
}
