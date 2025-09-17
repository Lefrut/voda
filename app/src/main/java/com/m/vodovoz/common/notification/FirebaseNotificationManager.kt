package com.m.vodovoz.common.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.m.vodovoz.R
import com.m.vodovoz.core.android.getBitmap
import com.m.vodovoz.data.parser.common.safeString
import com.m.vodovoz.ui.base.MainActivity
import com.m.vodovoz.util.extensions.fromHtml
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


        with(messageNotification) {
            if (imageUrl != null) {
                showLargeIconNotification(
                    image = imageUrl?.toString() ?: "",
                    title = title ?: "",
                    body = body ?: "",
                    data = jsonData
                )
            } else {
                showSmallIconNotification(
                    title = title ?: "",
                    body = body ?: "",
                    data = jsonData
                )
            }
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

    fun showLargeIconNotification(
        image: String,
        title: String,
        body: String,
        data: JSONObject,
    ) {


        val pendingIntent = createNotificationPendingIntent(data)

        val bitmap = getBitmap(image)
        val bigPictureStyle = NotificationCompat.BigPictureStyle().also {
            it.setBigContentTitle(title)
            it.setSummaryText(body.fromHtml())
            it.bigPicture(bitmap)
        }

        val notification =
            NotificationCompat.Builder(
                applicationContext,
                NotificationChannels.NOTIFICATION_CHANNEL_ID
            )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.pic_logo_notifications)
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .setLargeIcon(bitmap)
                .setStyle(bigPictureStyle)
                .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(applicationContext)
                    .notify(NotificationConfig.NOTIFICATION_ID_BIG_IMAGE, notification)
            }
        } else {
            NotificationManagerCompat.from(applicationContext)
                .notify(NotificationConfig.NOTIFICATION_ID_BIG_IMAGE, notification)
        }
    }

    fun showSmallIconNotification(
        title: String?,
        body: String?,
        data: JSONObject,
    ) {
        val pendingIntent = createNotificationPendingIntent(data)

        val inboxStyle = NotificationCompat.InboxStyle().also {
            it.addLine(body)
        }

        val notification =
            NotificationCompat.Builder(
                applicationContext,
                NotificationChannels.NOTIFICATION_CHANNEL_ID
            )
                .setContentTitle(title)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.pic_logo_notifications)
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .setStyle(inboxStyle)
                .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(applicationContext)
                    .notify(NotificationConfig.NOTIFICATION_ID, notification)
            }
        } else {
            NotificationManagerCompat.from(applicationContext)
                .notify(NotificationConfig.NOTIFICATION_ID, notification)
        }
    }
}