package com.stack.core.util

object FileUtil {
    private val audioExtensions = setOf(
        "mp3", "flac", "m4a", "ogg", "opus", "wav", "aac", "wma", "aiff"
    )

    fun getExtension(fileName: String): String {
        return fileName.substringAfterLast('.', "").lowercase()
    }

    fun getNameWithoutExtension(fileName: String): String {
        return fileName.substringBeforeLast('.')
    }

    fun isAudioFile(fileName: String): Boolean {
        return getExtension(fileName) in audioExtensions
    }

    fun isLrcFile(fileName: String): Boolean {
        return getExtension(fileName) == "lrc"
    }
}
