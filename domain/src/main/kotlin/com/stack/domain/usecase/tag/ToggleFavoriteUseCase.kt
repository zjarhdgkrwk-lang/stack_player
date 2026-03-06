package com.stack.domain.usecase.tag

import com.stack.domain.model.enums.SystemTagType
import com.stack.domain.repository.TagRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(trackId: Long): Boolean {
        val favoriteTag = tagRepository.getTagBySystemType(SystemTagType.FAVORITE) ?: return false
        val isFavorite = tagRepository.isTrackTagged(trackId, favoriteTag.id)
        if (isFavorite) {
            tagRepository.removeTagFromTrack(trackId, favoriteTag.id)
        } else {
            tagRepository.assignTagToTrack(trackId, favoriteTag.id)
        }
        return !isFavorite
    }
}
