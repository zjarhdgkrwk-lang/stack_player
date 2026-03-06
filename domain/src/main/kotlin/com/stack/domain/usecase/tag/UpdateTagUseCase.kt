package com.stack.domain.usecase.tag

import com.stack.domain.repository.TagRepository
import javax.inject.Inject

class UpdateTagUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(tagId: Long, name: String, color: Int) {
        val tag = tagRepository.getTagById(tagId) ?: return
        if (tag.isSystem) return
        tagRepository.updateTag(
            tag.copy(name = name, color = color, updatedAt = System.currentTimeMillis())
        )
    }
}
