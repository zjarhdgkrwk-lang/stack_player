package com.stack.domain.usecase.library

import javax.inject.Inject

class GetTracksUseCase @Inject constructor() {
    suspend operator fun invoke(sortOrder: com.stack.domain.model.enums.SortOrder = com.stack.domain.model.enums.SortOrder.DATE_ADDED_DESC): kotlinx.coroutines.flow.Flow<List<com.stack.domain.model.Track>> {
        TODO("Not yet implemented")
    }
}
