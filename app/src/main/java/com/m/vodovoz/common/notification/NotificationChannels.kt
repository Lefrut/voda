package com.m.vodovoz.common.notification

import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat

object NotificationChannels {

    const val NOTIFICATION_CHANNEL_ID = "NOTIFICATION_CHANNEL_ID"
    private const val NOTIFICATION_CHANNEL_NAME = "Reminders notification channel"

    fun create(context: Context) {
        createMainChannel(context)
    }

    private fun createMainChannel(context: Context) {
        val name = NOTIFICATION_CHANNEL_NAME
        val priority = NotificationManager.IMPORTANCE_HIGH

        val channel = android.app.NotificationChannel(NOTIFICATION_CHANNEL_ID, name, priority).apply {
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
        }
        NotificationManagerCompat.from(context).createNotificationChannel(channel)
    }
}