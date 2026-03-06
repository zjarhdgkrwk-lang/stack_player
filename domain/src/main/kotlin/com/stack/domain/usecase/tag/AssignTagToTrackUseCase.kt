package com.stack.domain.usecase.tag

import com.stack.domain.repository.TagRepository
import javax.inject.Inject

class AssignTagToTrackUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(trackId: Long, tagId: Long) {
        tagRepository.assignTagToTrack(trackId, tagId)
    }
}
