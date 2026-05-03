package com.poriot.aegis.ui

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.poriot.aegis.AegisApp
import com.poriot.aegis.data.PrefsManager
import com.poriot.aegis.databinding.ActivityMainBinding
import com.poriot.aegis.service.UsageMonitorService
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: PrefsManager
    private lateinit var adapter: AppUsageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PrefsManager(this)

        // First launch check
        if (prefs.isFirstLaunch) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        setupUI()
        checkPermissions()
        startMonitoring()
    }

    private fun setupUI() {
        adapter = AppUsageAdapter()
        binding.recyclerApps.layoutManager = LinearLayoutManager(this)
        binding.recyclerApps.adapter = adapter

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.switchLockMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.lockMode = if (isChecked) PrefsManager.MODE_NUCLEAR else PrefsManager.MODE_PARENT
        }

        binding.switchLockMode.isChecked = prefs.lockMode == PrefsManager.MODE_NUCLEAR

        // Observe usage data
        lifecycleScope.launch {
            (application as AegisApp).repository.getTodayUsage().collectLatest { list ->
                adapter.submitList(list)
                val total = list.filter { !it.isWhitelisted }.sumOf { it.secondsToday }
                val limit = prefs.dailyGlobalLimitMinutes * 60L
                val percent = (total.toFloat() / limit * 100).coerceIn(0f, 100f)
                binding.progressTotal.progress = percent.toInt()
                binding.tvTotalUsage.text = "${total / 60}m / ${prefs.dailyGlobalLimitMinutes}m"
            }
        }
    }

    private fun checkPermissions() {
        if (!hasUsageStatsPermission()) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun startMonitoring() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, UsageMonitorService::class.java))
        } else {
            startService(Intent(this, UsageMonitorService::class.java))
        }
    }
}