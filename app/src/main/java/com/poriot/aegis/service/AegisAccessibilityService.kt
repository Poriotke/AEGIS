package com.poriot.aegis.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import com.poriot.aegis.data.PrefsManager

class AegisAccessibilityService : AccessibilityService() {

    private lateinit var prefs: PrefsManager
    private var lastPackage: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefs = PrefsManager(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName == lastPackage) return
        lastPackage = packageName

        // If locked and not whitelisted, force back to lock screen
        if (prefs.isLocked) {
            val lifeSupport = prefs.getLifeSupportApps()
            if (packageName !in lifeSupport && packageName != packageName) {
                val intent = Intent(this, com.poriot.aegis.ui.LockOverlayActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {}
}