package com.stack.data.repository

import com.stack.data.local.db.dao.TagDao
import com.stack.data.local.db.entity.TrackTagCrossRef
import com.stack.data.mapper.TagMapper
import com.stack.domain.model.Tag
import com.stack.domain.model.enums.SystemTagType
import com.stack.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepositoryImpl @Inject constructor(
    private val tagDao: TagDao
) : TagRepository {

    override fun getAllTags(): Flow<List<Tag>> {
        return tagDao.getAllTags().map { TagMapper.toDomainList(it) }
    }

    override fun getSystemTags(): Flow<List<Tag>> {
        return tagDao.getSystemTags().map { TagMapper.toDomainList(it) }
    }

    override fun getCustomTags(): Flow<List<Tag>> {
        return tagDao.getCustomTags().map { TagMapper.toDomainList(it) }
    }

    override fun getTagsForTrack(trackId: Long): Flow<List<Tag>> {
        return tagDao.getTagsForTrack(trackId).map { TagMapper.toDomainList(it) }
    }

    override suspend fun getTagById(id: Long): Tag? {
        return tagDao.getTagById(id)?.let { TagMapper.toDomain(it) }
    }

    override suspend fun getTagBySystemType(type: SystemTagType): Tag? {
        return tagDao.getTagBySystemType(type.name)?.let { TagMapper.toDomain(it) }
    }

    override suspend fun insertTag(tag: Tag): Long {
        return tagDao.insertTag(TagMapper.toEntity(tag))
    }

    override suspend fun updateTag(tag: Tag) {
        tagDao.updateTag(TagMapper.toEntity(tag))
    }

    override suspend fun deleteTag(id: Long) {
        tagDao.deleteTag(id)
    }

    override suspend fun assignTagToTrack(trackId: Long, tagId: Long) {
        tagDao.insertTrackTagCrossRef(
            TrackTagCrossRef(
                trackId = trackId,
                tagId = tagId,
                taggedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun removeTagFromTrack(trackId: Long, tagId: Long) {
        tagDao.deleteTrackTagCrossRef(trackId, tagId)
    }

    override suspend fun isTrackTagged(trackId: Long, tagId: Long): Boolean {
        return tagDao.isTrackTagged(trackId, tagId) > 0
    }

    override suspend fun clearTagTracks(tagId: Long) {
        tagDao.clearTagTracks(tagId)
    }

    override suspend fun getTrackIdsForTag(tagId: Long): List<Long> {
        return tagDao.getTrackIdsForTag(tagId)
    }
}
