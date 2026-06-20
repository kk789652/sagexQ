package com.tiswu.sagex.service

import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.lifecycle.LifecycleService
import dagger.hilt.android.AndroidEntryPoint

/**
 * Foreground service that keeps the app process warm so the notification listener
 * is less likely to be killed. Opt-in; controlled via [ServiceController] and the
 * keep-alive preference. START_STICKY so the OS recreates it after a kill.
 */
@AndroidEntryPoint
class SageXForegroundService : LifecycleService() {

    override fun onCreate() {
        super.onCreate()
        CaptureNotifications.ensureChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val notification = CaptureNotifications.build(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // specialUse: the honest type for keeping the capture process alive; also the only
            // category permitted to be (re)started from a BOOT_COMPLETED receiver on Android 14/15.
            startForeground(
                CaptureNotifications.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(CaptureNotifications.NOTIFICATION_ID, notification)
        }
        return START_STICKY
    }
}
