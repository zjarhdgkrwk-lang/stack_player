package com.stack.core.util

object StringUtil {
    fun truncate(text: String, maxLength: Int, suffix: String = "…"): String {
        return if (text.length <= maxLength) text
        else text.take(maxLength - suffix.length) + suffix
    }

    fun normalizeWhitespace(text: String): String {
        return text.trim().replace(Regex("\\s+"), " ")
    }
}
