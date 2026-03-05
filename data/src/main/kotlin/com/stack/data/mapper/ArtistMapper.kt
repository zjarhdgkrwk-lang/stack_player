package com.stack.data.mapper

import com.stack.data.local.db.entity.ArtistEntity
import com.stack.domain.model.Artist

object ArtistMapper {

    fun toDomain(entity: ArtistEntity): Artist = Artist(
        id = entity.id,
        name = entity.name,
        albumCount = entity.albumCount,
        trackCount = entity.trackCount
    )

    fun toEntity(domain: Artist): ArtistEntity = ArtistEntity(
        id = domain.id,
        name = domain.name,
        albumCount = domain.albumCount,
        trackCount = domain.trackCount
    )

    fun toDomainList(entities: List<ArtistEntity>): List<Artist> = entities.map { toDomain(it) }
}
