package com.poriot.aegis.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class AppUsageRepository(private val dao: AppUsageDao) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getTodayUsage(): Flow<List<AppUsageEntity>> {
        return dao.getAllForDate(getToday())
    }

    suspend fun getEntity(packageName: String): AppUsageEntity? {
        return dao.getByPackageAndDate(packageName, getToday())
    }

    suspend fun recordUsage(packageName: String, appName: String, seconds: Long) {
        val today = getToday()
        val existing = dao.getByPackageAndDate(packageName, today)
        if (existing != null) {
            dao.incrementUsage(packageName, today, seconds)
        } else {
            dao.insert(AppUsageEntity(
                packageName = packageName,
                appName = appName,
                date = today,
                secondsToday = seconds
            ))
        }
    }

    suspend fun setLimit(packageName: String, limitSeconds: Long) {
        val today = getToday()
        val existing = dao.getByPackageAndDate(packageName, today)
        if (existing != null) {
            dao.update(existing.copy(dailyLimitSeconds = limitSeconds))
        }
    }

    suspend fun whitelistApp(packageName: String) {
        val today = getToday()
        val existing = dao.getByPackageAndDate(packageName, today)
        if (existing != null) {
            dao.update(existing.copy(isWhitelisted = true))
        }
    }

    suspend fun getTotalNonWhitelistedUsage(): Long {
        return dao.getTotalNonWhitelistedUsage(getToday()) ?: 0L
    }

    suspend fun clearOldData() {
        dao.clearOldData(getToday())
    }

    private fun getToday(): String = dateFormat.format(Date())
}