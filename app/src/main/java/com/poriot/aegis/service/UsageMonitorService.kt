package com.poriot.aegis.service

import android.app.*
import android.app.usage.UsageStatsManager
import android.content.*
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.poriot.aegis.R
import com.poriot.aegis.data.AppUsageRepository
import com.poriot.aegis.data.PrefsManager
import com.poriot.aegis.ui.LockOverlayActivity
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class UsageMonitorService : LifecycleService() {

    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var prefs: PrefsManager
    private lateinit var repository: AppUsageRepository
    private val handler = Handler(Looper.getMainLooper())
    private var lastTrackedPackage: String? = null
    private var lastTrackedTime: Long = 0
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val monitorRunnable = object : Runnable {
        override fun run() {
            trackCurrentApp()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        prefs = PrefsManager(this)
        repository = com.poriot.aegis.AegisApp.instance.repository
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startForeground(NOTIFICATION_ID, buildNotification())
        handler.post(monitorRunnable)
        return START_STICKY
    }

    private fun trackCurrentApp() {
        val now = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            now - 60000,
            now
        ) ?: return

        val currentApp = stats.maxByOrNull { it.lastTimeUsed }?.packageName ?: return
        val currentTime = System.currentTimeMillis()

        if (lastTrackedPackage != null && lastTrackedPackage != currentApp) {
            val elapsed = (currentTime - lastTrackedTime) / 1000
            if (elapsed > 0) {
                recordUsage(lastTrackedPackage!!, elapsed)
            }
        }

        lastTrackedPackage = currentApp
        lastTrackedTime = currentTime

        // Check lock trigger
        lifecycleScope.launch(Dispatchers.IO) {
            checkLockTrigger()
        }
    }

    private suspend fun recordUsage(packageName: String, seconds: Long) {
        val appName = try {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(packageName, 0)
            ).toString()
        } catch (e: Exception) {
            packageName
        }
        repository.recordUsage(packageName, appName, seconds)
    }

    private suspend fun checkLockTrigger() {
        if (prefs.isLocked) return

        val totalUsage = repository.getTotalNonWhitelistedUsage()
        val limitSeconds = prefs.dailyGlobalLimitMinutes * 60L

        if (totalUsage >= limitSeconds) {
            prefs.isLocked = true
            triggerLock()
        }
    }

    private fun triggerLock() {
        val intent = Intent(this, LockOverlayActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("mode", prefs.lockMode)
        }
        startActivity(intent)
    }

    private fun buildNotification(): Notification {
        val channelId = "aegis_monitor_channel"
        val channel = NotificationChannel(
            channelId,
            "AEGIS Monitor",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Real-time screen time tracking" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("AEGIS is watching")
            .setContentText("Monitoring your digital discipline...")
            .setSmallIcon(R.drawable.ic_shield)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    override fun onDestroy() {
        handler.removeCallbacks(monitorRunnable)
        // Schedule restart
        sendBroadcast(Intent("com.poriot.aegis.ACTION_RESTART_SERVICE"))
        super.onDestroy()
    }

    companion object {
        private const val NOTIFICATION_ID = 1337
        private const val TAG = "UsageMonitorService"
    }
}