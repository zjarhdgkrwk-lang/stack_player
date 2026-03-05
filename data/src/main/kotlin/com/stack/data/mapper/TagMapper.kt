package com.stack.data.mapper

import com.stack.data.local.db.entity.TagEntity
import com.stack.domain.model.Tag
import com.stack.domain.model.enums.SystemTagType

object TagMapper {

    fun toDomain(entity: TagEntity): Tag = Tag(
        id = entity.id,
        name = entity.name,
        color = entity.color,
        isSystem = entity.isSystem,
        systemType = entity.systemType?.let { SystemTagType.valueOf(it) },
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )

    fun toEntity(domain: Tag): TagEntity = TagEntity(
        id = domain.id,
        name = domain.name,
        color = domain.color,
        isSystem = domain.isSystem,
        systemType = domain.systemType?.name,
        createdAt = domain.createdAt,
        updatedAt = domain.updatedAt
    )

    fun toDomainList(entities: List<TagEntity>): List<Tag> = entities.map { toDomain(it) }
}
