package com.stack.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.stack.data.local.db.entity.TagEntity
import com.stack.data.local.db.entity.TrackTagCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Query("SELECT * FROM tags ORDER BY isSystem DESC, name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE isSystem = 1 ORDER BY name ASC")
    fun getSystemTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE isSystem = 0 ORDER BY name ASC")
    fun getCustomTags(): Flow<List<TagEntity>>

    @Query("""
        SELECT tags.* FROM tags
        INNER JOIN track_tag_cross_ref ON tags.id = track_tag_cross_ref.tagId
        WHERE track_tag_cross_ref.trackId = :trackId
        ORDER BY tags.isSystem DESC, tags.name ASC
    """)
    fun getTagsForTrack(trackId: Long): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getTagById(id: Long): TagEntity?

    @Query("SELECT * FROM tags WHERE systemType = :systemType LIMIT 1")
    suspend fun getTagBySystemType(systemType: String): TagEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity): Long

    @Update
    suspend fun updateTag(tag: TagEntity)

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteTag(id: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrackTagCrossRef(crossRef: TrackTagCrossRef)

    @Query("DELETE FROM track_tag_cross_ref WHERE trackId = :trackId AND tagId = :tagId")
    suspend fun deleteTrackTagCrossRef(trackId: Long, tagId: Long)

    @Query("SELECT COUNT(*) FROM track_tag_cross_ref WHERE trackId = :trackId AND tagId = :tagId")
    suspend fun isTrackTagged(trackId: Long, tagId: Long): Int

    @Query("DELETE FROM track_tag_cross_ref WHERE tagId = :tagId")
    suspend fun clearTagTracks(tagId: Long)

    @Query("SELECT trackId FROM track_tag_cross_ref WHERE tagId = :tagId")
    suspend fun getTrackIdsForTag(tagId: Long): List<Long>

    @Query("SELECT COUNT(*) FROM track_tag_cross_ref WHERE tagId = :tagId")
    suspend fun getTrackCountForTag(tagId: Long): Int
}
