package com.tiswu.sagex.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.PendingIntentCompat
import android.app.PendingIntent
import com.tiswu.sagex.MainActivity
import com.tiswu.sagex.R

/** Persistent foreground-service notification for the keep-alive service. */
object CaptureNotifications {

    const val CHANNEL_ID = "capture_status"
    const val NOTIFICATION_ID = 1001

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "捕获服务",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "保持消息捕获在后台运行"
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun build(context: Context): Notification {
        val contentIntent = PendingIntentCompat.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT,
            false,
        )
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText("正在后台保持消息捕获")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
