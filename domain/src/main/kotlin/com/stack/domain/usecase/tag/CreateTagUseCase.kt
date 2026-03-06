package com.stack.domain.usecase.tag

import com.stack.domain.model.Tag
import com.stack.domain.repository.TagRepository
import javax.inject.Inject

class CreateTagUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(name: String, color: Int): Long {
        val now = System.currentTimeMillis()
        return tagRepository.insertTag(
            Tag(
                id = 0,
                name = name,
                color = color,
                isSystem = false,
                systemType = null,
                createdAt = now,
                updatedAt = now
            )
        )
    }
}
