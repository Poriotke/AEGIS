package com.poriot.aegis.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.poriot.aegis.service.UsageMonitorService

class ServiceRestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.poriot.aegis.ACTION_RESTART_SERVICE") {
            context.startForegroundService(
                Intent(context, UsageMonitorService::class.java)
            )
        }
    }
}