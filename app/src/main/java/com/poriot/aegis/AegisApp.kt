package com.poriot.aegis

import android.app.Application
import androidx.room.Room
import com.poriot.aegis.data.AppDatabase
import com.poriot.aegis.data.AppUsageRepository

class AegisApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: AppUsageRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "aegis_database"
        ).build()

        repository = AppUsageRepository(database.appUsageDao())
    }

    companion object {
        lateinit var instance: AegisApp
            private set
    }
}