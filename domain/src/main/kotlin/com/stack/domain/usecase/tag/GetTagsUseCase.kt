package com.stack.domain.usecase.tag

import com.stack.domain.model.Tag
import com.stack.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTagsUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    operator fun invoke(): Flow<List<Tag>> = tagRepository.getAllTags()

    fun systemTags(): Flow<List<Tag>> = tagRepository.getSystemTags()

    fun customTags(): Flow<List<Tag>> = tagRepository.getCustomTags()

    fun forTrack(trackId: Long): Flow<List<Tag>> = tagRepository.getTagsForTrack(trackId)
}
