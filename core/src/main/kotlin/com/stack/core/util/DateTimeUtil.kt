package com.stack.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtil {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun formatDate(epochMs: Long): String = dateFormat.format(Date(epochMs))

    fun formatDateTime(epochMs: Long): String = dateTimeFormat.format(Date(epochMs))

    fun currentEpochMs(): Long = System.currentTimeMillis()
}
