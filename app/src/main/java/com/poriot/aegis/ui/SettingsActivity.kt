package com.poriot.aegis.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.poriot.aegis.data.PrefsManager
import com.poriot.aegis.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PrefsManager(this)

        binding.etGlobalLimit.setText(prefs.dailyGlobalLimitMinutes.toString())
        binding.etParentPin.setText(prefs.parentPin ?: "")
        binding.switchMode.isChecked = prefs.lockMode == PrefsManager.MODE_NUCLEAR

        binding.btnSave.setOnClickListener {
            val limit = binding.etGlobalLimit.text.toString().toIntOrNull() ?: 120
            val pin = binding.etParentPin.text.toString()
            val mode = if (binding.switchMode.isChecked) PrefsManager.MODE_NUCLEAR else PrefsManager.MODE_PARENT

            prefs.dailyGlobalLimitMinutes = limit
            if (pin.length in 4..6) prefs.parentPin = pin
            prefs.lockMode = mode

            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}