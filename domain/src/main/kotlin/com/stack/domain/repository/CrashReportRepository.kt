package com.stack.domain.repository

import com.stack.domain.model.CrashReport
import kotlinx.coroutines.flow.Flow

interface CrashReportRepository {
    fun getAllCrashReports(): Flow<List<CrashReport>>
    suspend fun insertCrashReport(report: CrashReport)
    suspend fun deleteCrashReport(id: Long)
    suspend fun pruneOldReports(maxCount: Int = 50)
}
