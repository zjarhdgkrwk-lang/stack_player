package com.stack.domain.usecase.tag

import com.stack.domain.model.enums.SystemTagType
import com.stack.domain.repository.PlayHistoryRepository
import com.stack.domain.repository.TagRepository
import com.stack.domain.repository.TrackRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class RefreshSystemTagsUseCase @Inject constructor(
    private val tagRepository: TagRepository,
    private val trackRepository: TrackRepository,
    private val playHistoryRepository: PlayHistoryRepository
) {
    suspend operator fun invoke() {
        refreshRecentPlay()
        refreshRecentAdd()
        refreshMostPlayed()
    }

    private suspend fun refreshRecentPlay() {
        val tag = tagRepository.getTagBySystemType(SystemTagType.RECENT_PLAY) ?: return
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        val recentHistory = playHistoryRepository.getRecentHistory(1000).first()
        val recentTrackIds = recentHistory
            .filter { it.playedAt >= thirtyDaysAgo }
            .map { it.trackId }
            .distinct()

        updateTagTracks(tag.id, recentTrackIds)
    }

    private suspend fun refreshRecentAdd() {
        val tag = tagRepository.getTagBySystemType(SystemTagType.RECENT_ADD) ?: return
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        val allTracks = trackRepository.getAllTracks().first()
        val recentTrackIds = allTracks
            .filter { it.dateAdded >= thirtyDaysAgo }
            .map { it.id }

        updateTagTracks(tag.id, recentTrackIds)
    }

    private suspend fun refreshMostPlayed() {
        val tag = tagRepository.getTagBySystemType(SystemTagType.MOST_PLAYED) ?: return
        val topTrackIds = playHistoryRepository.getMostPlayedTrackIds(100).first()

        updateTagTracks(tag.id, topTrackIds)
    }

    private suspend fun updateTagTracks(tagId: Long, trackIds: List<Long>) {
        tagRepository.clearTagTracks(tagId)
        trackIds.forEach { trackId ->
            tagRepository.assignTagToTrack(trackId, tagId)
        }
    }
}
