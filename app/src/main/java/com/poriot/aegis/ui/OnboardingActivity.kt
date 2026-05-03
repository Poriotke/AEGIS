package com.poriot.aegis.ui

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.poriot.aegis.data.AppUsageEntity
import com.poriot.aegis.data.PrefsManager
import com.poriot.aegis.databinding.ActivityOnboardingBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var prefs: PrefsManager
    private val selectedApps = mutableSetOf<String>()
    private val lifeSupportApps = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PrefsManager(this)

        // Mandatory defaults
        lifeSupportApps.addAll(PrefsManager.DEFAULT_LIFE_SUPPORT)
        selectedApps.addAll(PrefsManager.DEFAULT_LIFE_SUPPORT)

        setupAppSelector()
        setupButtons()
    }

    private fun setupAppSelector() {
        val pm = packageManager
        val apps = pm.getInstalledApplications(0)
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 || it.packageName in PrefsManager.DEFAULT_LIFE_SUPPORT }
            .sortedBy { pm.getApplicationLabel(it).toString() }

        val adapter = AppSelectorAdapter(apps, selectedApps) { packageName, isSelected ->
            if (isSelected && packageName !in PrefsManager.DEFAULT_LIFE_SUPPORT) {
                if (selectedApps.count { it !in PrefsManager.DEFAULT_LIFE_SUPPORT } >= 3) {
                    Toast.makeText(this, "Max 3 additional Life-Support apps", Toast.LENGTH_SHORT).show()
                    return@AppSelectorAdapter
                }
            }
            if (isSelected) selectedApps.add(packageName) else selectedApps.remove(packageName)
        }

        binding.recyclerSelectApps.layoutManager = LinearLayoutManager(this)
        binding.recyclerSelectApps.adapter = adapter
    }

    private fun setupButtons() {
        binding.btnContinue.setOnClickListener {
            if (selectedApps.isEmpty()) {
                Toast.makeText(this, "Select at least Life-Support apps", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            prefs.setLifeSupportApps(selectedApps)
            prefs.isFirstLaunch = false

            // Pre-populate database
            lifecycleScope.launch {
                val repo = com.poriot.aegis.AegisApp.instance.repository
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                selectedApps.forEach { pkg ->
                    val name = try {
                        packageManager.getApplicationLabel(
                            packageManager.getApplicationInfo(pkg, 0)
                        ).toString()
                    } catch (e: Exception) { pkg }
                    repo.database.appUsageDao().insert(
                        AppUsageEntity(
                            packageName = pkg,
                            appName = name,
                            date = date,
                            isWhitelisted = true,
                            isLifeSupport = pkg in PrefsManager.DEFAULT_LIFE_SUPPORT
                        )
                    )
                }

                startActivity(Intent(this@OnboardingActivity, MainActivity::class.java))
                finish()
            }
        }
    }
}