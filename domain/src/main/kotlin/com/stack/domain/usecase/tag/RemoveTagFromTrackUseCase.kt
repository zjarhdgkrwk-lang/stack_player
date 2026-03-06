package com.stack.domain.usecase.tag

import com.stack.domain.repository.TagRepository
import javax.inject.Inject

class RemoveTagFromTrackUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(trackId: Long, tagId: Long) {
        tagRepository.removeTagFromTrack(trackId, tagId)
    }
}
