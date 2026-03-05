package com.stack.domain.repository

import com.stack.domain.model.ScanProgress
import kotlinx.coroutines.flow.StateFlow

interface ScanService {
    val scanProgress: StateFlow<ScanProgress>
    suspend fun fullScan()
    suspend fun incrementalScan()
    suspend fun scanFolder(folderId: Long)
    suspend fun cleanGhostTracks()
    fun cancelScan()
}
