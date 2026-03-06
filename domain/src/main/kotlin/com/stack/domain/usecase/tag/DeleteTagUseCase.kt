package com.stack.domain.usecase.tag

import com.stack.domain.repository.TagRepository
import javax.inject.Inject

class DeleteTagUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(tagId: Long) {
        val tag = tagRepository.getTagById(tagId) ?: return
        if (tag.isSystem) return
        tagRepository.deleteTag(tagId)
    }
}
