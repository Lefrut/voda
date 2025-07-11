package com.vodovoz.app.common.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vodovoz.app.R
import com.vodovoz.app.core.android.getBitmap
import com.vodovoz.app.data.parser.common.safeString
import com.vodovoz.app.ui.base.MainActivity
import com.vodovoz.app.util.extensions.fromHtml
import org.json.JSONObject

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class FirebaseNotificationManager : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val messageNotification = message.notification ?: return
        val params = message.data.toMap()

        val jsonData = if (params.isNotEmpty()) {
            JSONObject(params)
        } else {
            JSONObject()
        }

        if (messageNotification.imageUrl != null) {
            showLargeIconNotification(messageNotification, jsonData)
        } else {
            showSmallIconNotification(messageNotification, jsonData)
        }

    }

    private fun createNotificationPendingIntent(data: JSONObject): PendingIntent {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data.keys().forEach { key -> putExtra(key, data.safeString(key)) }
        }

        return PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun showLargeIconNotification(not: RemoteMessage.Notification, data: JSONObject) {


        val pendingIntent = createNotificationPendingIntent(data)

        val bitmap = getBitmap(not.imageUrl.toString())
        val bigPictureStyle = NotificationCompat.BigPictureStyle().also {
            it.setBigContentTitle(not.title)
            it.setSummaryText(not.body?.fromHtml())
            it.bigPicture(bitmap)
        }

        val notification =
            NotificationCompat.Builder(this, NotificationChannels.NOTIFICATION_CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.pic_logo_notifications)
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .setLargeIcon(bitmap)
                .setStyle(bigPictureStyle)
                .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(this)
                    .notify(NotificationConfig.NOTIFICATION_ID_BIG_IMAGE, notification)
            }
        } else {
            NotificationManagerCompat.from(this)
                .notify(NotificationConfig.NOTIFICATION_ID_BIG_IMAGE, notification)
        }
    }

    private fun showSmallIconNotification(not: RemoteMessage.Notification, data: JSONObject) {
        val pendingIntent = createNotificationPendingIntent(data)

        val inboxStyle = NotificationCompat.InboxStyle().also {
            it.addLine(not.body)
        }

        val notification =
            NotificationCompat.Builder(this, NotificationChannels.NOTIFICATION_CHANNEL_ID)
                .setContentTitle(not.title)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.pic_logo_notifications)
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .setStyle(inboxStyle)
                .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(this)
                    .notify(NotificationConfig.NOTIFICATION_ID, notification)
            }
        } else {
            NotificationManagerCompat.from(this)
                .notify(NotificationConfig.NOTIFICATION_ID, notification)
        }
    }
}