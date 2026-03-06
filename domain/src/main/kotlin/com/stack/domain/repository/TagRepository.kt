package com.stack.domain.repository

import com.stack.domain.model.Tag
import com.stack.domain.model.enums.SystemTagType
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun getAllTags(): Flow<List<Tag>>
    fun getSystemTags(): Flow<List<Tag>>
    fun getCustomTags(): Flow<List<Tag>>
    fun getTagsForTrack(trackId: Long): Flow<List<Tag>>
    suspend fun getTagById(id: Long): Tag?
    suspend fun getTagBySystemType(type: SystemTagType): Tag?
    suspend fun insertTag(tag: Tag): Long
    suspend fun updateTag(tag: Tag)
    suspend fun deleteTag(id: Long)
    suspend fun assignTagToTrack(trackId: Long, tagId: Long)
    suspend fun removeTagFromTrack(trackId: Long, tagId: Long)
    suspend fun isTrackTagged(trackId: Long, tagId: Long): Boolean
    suspend fun clearTagTracks(tagId: Long)
    suspend fun getTrackIdsForTag(tagId: Long): List<Long>
}
