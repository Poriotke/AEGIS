package com.poriot.aegis.data

import android.content.Context
import android.content.SharedPreferences

class PrefsManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()

    var lockMode: String
        get() = prefs.getString(KEY_LOCK_MODE, MODE_PARENT) ?: MODE_PARENT
        set(value) = prefs.edit().putString(KEY_LOCK_MODE, value).apply()

    var parentPin: String?
        get() = prefs.getString(KEY_PARENT_PIN, null)
        set(value) = prefs.edit().putString(KEY_PARENT_PIN, value).apply()

    var dailyGlobalLimitMinutes: Int
        get() = prefs.getInt(KEY_GLOBAL_LIMIT, 120)
        set(value) = prefs.edit().putInt(KEY_GLOBAL_LIMIT, value).apply()

    var isLocked: Boolean
        get() = prefs.getBoolean(KEY_IS_LOCKED, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOCKED, value).apply()

    fun getLifeSupportApps(): Set<String> {
        return prefs.getStringSet(KEY_LIFE_SUPPORT, DEFAULT_LIFE_SUPPORT) ?: DEFAULT_LIFE_SUPPORT
    }

    fun setLifeSupportApps(apps: Set<String>) {
        prefs.edit().putStringSet(KEY_LIFE_SUPPORT, apps).apply()
    }

    companion object {
        private const val PREFS_NAME = "aegis_prefs"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_LOCK_MODE = "lock_mode"
        private const val KEY_PARENT_PIN = "parent_pin"
        private const val KEY_GLOBAL_LIMIT = "global_limit"
        private const val KEY_IS_LOCKED = "is_locked"
        private const val KEY_LIFE_SUPPORT = "life_support_apps"

        const val MODE_PARENT = "parent"
        const val MODE_NUCLEAR = "nuclear"

        val DEFAULT_LIFE_SUPPORT = setOf(
            "com.android.dialer",
            "com.google.android.dialer",
            "com.android.messaging",
            "com.google.android.apps.messaging",
            "com.android.clock",
            "com.google.android.deskclock",
            "com.android.settings",
            "com.google.android.apps.maps"
        )
    }
}