package com.stack.data.repository

import com.stack.data.local.db.dao.CrashReportDao
import com.stack.data.local.db.entity.CrashReportEntity
import com.stack.domain.model.CrashReport
import com.stack.domain.repository.CrashReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrashReportRepositoryImpl @Inject constructor(
    private val crashReportDao: CrashReportDao
) : CrashReportRepository {

    override fun getAllCrashReports(): Flow<List<CrashReport>> {
        return crashReportDao.getAllCrashReports().map { entities ->
            entities.map { entity ->
                CrashReport(
                    id = entity.id,
                    createdAt = entity.createdAt,
                    throwableSummary = entity.throwableSummary,
                    stacktrace = entity.stacktrace,
                    appVersion = entity.appVersion,
                    deviceInfo = entity.deviceInfo
                )
            }
        }
    }

    override suspend fun insertCrashReport(report: CrashReport) {
        crashReportDao.insertCrashReport(
            CrashReportEntity(
                id = report.id,
                createdAt = report.createdAt,
                throwableSummary = report.throwableSummary,
                stacktrace = report.stacktrace,
                appVersion = report.appVersion,
                deviceInfo = report.deviceInfo
            )
        )
    }

    override suspend fun deleteCrashReport(id: Long) {
        crashReportDao.deleteCrashReport(id)
    }

    override suspend fun pruneOldReports(maxCount: Int) {
        crashReportDao.pruneOldReports(maxCount)
    }
}
