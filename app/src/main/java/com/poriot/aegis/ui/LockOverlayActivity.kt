package com.poriot.aegis.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.poriot.aegis.data.PrefsManager
import com.poriot.aegis.databinding.ActivityLockOverlayBinding

class LockOverlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockOverlayBinding
    private lateinit var prefs: PrefsManager
    private var pinBuffer = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLockOverlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PrefsManager(this)
        val mode = intent.getStringExtra("mode") ?: prefs.lockMode

        // Prevent dismissal
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        setupLockUI(mode)
        setupEmergencyButton()
    }

    private fun setupLockUI(mode: String) {
        when (mode) {
            PrefsManager.MODE_NUCLEAR -> {
                binding.tvLockTitle.text = "☢️ NUCLEAR LOCK ACTIVE"
                binding.tvLockSubtitle.text = "Zero-override discipline enforced.
Only Life-Support apps accessible."
                binding.pinPad.visibility = android.view.View.GONE
                binding.tvLockTitle.setTextColor(android.graphics.Color.parseColor("#FF4444"))
            }
            else -> {
                binding.tvLockTitle.text = "🔒 PARENT MODE"
                binding.tvLockSubtitle.text = "Enter PIN to extend session"
                setupPinPad()
            }
        }
    }

    private fun setupPinPad() {
        val digits = listOf(
            binding.btn0, binding.btn1, binding.btn2, binding.btn3, binding.btn4,
            binding.btn5, binding.btn6, binding.btn7, binding.btn8, binding.btn9
        )
        digits.forEachIndexed { index, btn ->
            btn.setOnClickListener { appendPin(index.toString()) }
        }
        binding.btnClear.setOnClickListener { clearPin() }
        binding.btnEnter.setOnClickListener { validatePin() }
    }

    private fun appendPin(digit: String) {
        if (pinBuffer.length < 6) {
            pinBuffer += digit
            updatePinDisplay()
        }
    }

    private fun clearPin() {
        pinBuffer = ""
        updatePinDisplay()
    }

    private fun updatePinDisplay() {
        binding.tvPinDisplay.text = "●".repeat(pinBuffer.length).padEnd(6, '○')
    }

    private fun validatePin() {
        if (pinBuffer == prefs.parentPin) {
            prefs.isLocked = false
            Toast.makeText(this, "Access granted", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            pinBuffer = ""
            updatePinDisplay()
            Toast.makeText(this, "Invalid PIN", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupEmergencyButton() {
        binding.btnEmergency.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:911")
            }
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        // Block back button
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_HOME,
            KeyEvent.KEYCODE_APP_SWITCH -> true
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus && prefs.isLocked) {
            // Re-launch if user tries to escape
            val intent = Intent(this, LockOverlayActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
        }
    }
}