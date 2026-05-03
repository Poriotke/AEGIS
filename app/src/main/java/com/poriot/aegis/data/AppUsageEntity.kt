package com.poriot.aegis.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_usage")
data class AppUsageEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val date: String,           // yyyy-MM-dd
    val secondsToday: Long = 0,
    val dailyLimitSeconds: Long = 7200,  // Default 2 hours
    val isWhitelisted: Boolean = false,
    val isLifeSupport: Boolean = false
)