package com.stack.data.mapper

import com.stack.data.local.db.entity.LyricsEntity
import com.stack.domain.model.Lyrics
import com.stack.domain.model.LyricsLine
import com.stack.domain.model.enums.LyricsSyncType

object LyricsMapper {

    fun toDomain(entity: LyricsEntity): Lyrics {
        val lines = if (entity.syncType == "SYNCED") {
            parseLrcLines(entity.content)
        } else {
            emptyList()
        }
        return Lyrics(
            trackId = entity.trackId,
            content = entity.content,
            syncType = LyricsSyncType.valueOf(entity.syncType),
            source = entity.source,
            lrcFilePath = entity.lrcFilePath,
            lines = lines,
            updatedAt = entity.updatedAt
        )
    }

    fun toEntity(domain: Lyrics): LyricsEntity = LyricsEntity(
        trackId = domain.trackId,
        content = domain.content,
        syncType = domain.syncType.name,
        source = domain.source,
        lrcFilePath = domain.lrcFilePath,
        updatedAt = domain.updatedAt
    )

    private fun parseLrcLines(content: String): List<LyricsLine> {
        val pattern = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})](.*)""")
        return content.lines().mapNotNull { line ->
            pattern.find(line)?.let { match ->
                val minutes = match.groupValues[1].toLongOrNull() ?: return@let null
                val seconds = match.groupValues[2].toLongOrNull() ?: return@let null
                val milliStr = match.groupValues[3]
                val millis = when (milliStr.length) {
                    2 -> (milliStr.toLongOrNull() ?: 0) * 10
                    3 -> milliStr.toLongOrNull() ?: 0
                    else -> 0
                }
                val timestampMs = minutes * 60_000 + seconds * 1000 + millis
                val text = match.groupValues[4]
                LyricsLine(timestampMs = timestampMs, text = text)
            }
        }.sortedBy { it.timestampMs }
    }
}
