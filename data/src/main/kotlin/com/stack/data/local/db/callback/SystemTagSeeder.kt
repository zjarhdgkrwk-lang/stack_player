package com.stack.data.local.db.callback

import com.stack.data.local.db.dao.TagDao
import com.stack.data.local.db.entity.TagEntity
import com.stack.domain.model.enums.SystemTagType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemTagSeeder @Inject constructor(
    private val tagDao: TagDao
) {
    suspend fun seedIfNeeded() {
        SystemTagType.entries.forEach { type ->
            val existing = tagDao.getTagBySystemType(type.name)
            if (existing == null) {
                val now = System.currentTimeMillis()
                tagDao.insertTag(
                    TagEntity(
                        name = type.displayName(),
                        color = type.defaultColor(),
                        isSystem = true,
                        systemType = type.name,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            }
        }
    }

    private fun SystemTagType.displayName(): String = when (this) {
        SystemTagType.FAVORITE -> "Favorites"
        SystemTagType.RECENT_PLAY -> "Recently Played"
        SystemTagType.RECENT_ADD -> "Recently Added"
        SystemTagType.MOST_PLAYED -> "Most Played"
    }

    private fun SystemTagType.defaultColor(): Int = when (this) {
        SystemTagType.FAVORITE -> 0xFFE57373.toInt()
        SystemTagType.RECENT_PLAY -> 0xFF64B5F6.toInt()
        SystemTagType.RECENT_ADD -> 0xFF81C784.toInt()
        SystemTagType.MOST_PLAYED -> 0xFFFFD54F.toInt()
    }
}
