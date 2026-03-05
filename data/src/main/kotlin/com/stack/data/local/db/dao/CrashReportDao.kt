package com.stack.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.stack.data.local.db.entity.CrashReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CrashReportDao {

    @Query("SELECT * FROM crash_reports ORDER BY createdAt DESC")
    fun getAllCrashReports(): Flow<List<CrashReportEntity>>

    @Insert
    suspend fun insertCrashReport(report: CrashReportEntity)

    @Query("DELETE FROM crash_reports WHERE id = :id")
    suspend fun deleteCrashReport(id: Long)

    @Query("""
        DELETE FROM crash_reports WHERE id NOT IN (
            SELECT id FROM crash_reports ORDER BY createdAt DESC LIMIT :maxCount
        )
    """)
    suspend fun pruneOldReports(maxCount: Int)
}
