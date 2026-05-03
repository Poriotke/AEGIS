package com.poriot.aegis.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageDao {

    @Query("SELECT * FROM app_usage WHERE date = :date ORDER BY secondsToday DESC")
    fun getAllForDate(date: String): Flow<List<AppUsageEntity>>

    @Query("SELECT * FROM app_usage WHERE packageName = :packageName AND date = :date LIMIT 1")
    suspend fun getByPackageAndDate(packageName: String, date: String): AppUsageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AppUsageEntity)

    @Update
    suspend fun update(entity: AppUsageEntity)

    @Query("UPDATE app_usage SET secondsToday = secondsToday + :seconds WHERE packageName = :packageName AND date = :date")
    suspend fun incrementUsage(packageName: String, date: String, seconds: Long)

    @Query("SELECT SUM(secondsToday) FROM app_usage WHERE date = :date AND isWhitelisted = 0")
    suspend fun getTotalNonWhitelistedUsage(date: String): Long?

    @Query("SELECT * FROM app_usage WHERE isWhitelisted = 1")
    fun getWhitelistedApps(): Flow<List<AppUsageEntity>>

    @Query("DELETE FROM app_usage WHERE date != :date")
    suspend fun clearOldData(date: String)
}