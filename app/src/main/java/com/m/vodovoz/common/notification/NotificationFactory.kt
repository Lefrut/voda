package com.m.vodovoz.common.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.m.vodovoz.R
import com.m.vodovoz.core.android.getBitmap
import com.m.vodovoz.core.android.getNotificationPendingIntent
import com.m.vodovoz.util.extensions.fromHtml
import org.json.JSONObject

class NotificationFactory(
    private val applicationContext: Context
) {
    fun showLargeNotification(
        image: String,
        title: String,
        body: String,
        data: JSONObject,
    ) {
        val pendingIntent = applicationContext.getNotificationPendingIntent(data)
        val largeImageBitmap = applicationContext.getBitmap(image)

        val bigPictureStyle = NotificationCompat.BigPictureStyle().also { style ->
            style.setBigContentTitle(title)
            style.setSummaryText(body.fromHtml())
            style.bigPicture(largeImageBitmap)
        }

        showNotification(
            notificationId = NotificationConfig.NOTIFICATION_ID_BIG_IMAGE,
            pendingIntent = pendingIntent
        ) {
            setLargeIcon(largeImageBitmap)
            setStyle(bigPictureStyle)
        }
    }

    fun showSmallNotification(
        title: String?,
        body: String?,
        data: JSONObject,
    ) {
        val pendingIntent = applicationContext.getNotificationPendingIntent(data)
        val inboxStyle = NotificationCompat.InboxStyle().also { style ->
            style.addLine(body)
        }

        showNotification(
            notificationId = NotificationConfig.NOTIFICATION_ID,
            pendingIntent = pendingIntent
        ) {
            setContentTitle(title)
            setStyle(inboxStyle)
        }
    }

    private fun showNotification(
        notificationId: Int,
        pendingIntent: PendingIntent,
        configure: NotificationCompat.Builder.() -> Unit,
    ) {
        val notification = NotificationCompat.Builder(
            applicationContext,
            NotificationChannels.NOTIFICATION_CHANNEL_ID
        ).apply {
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setSmallIcon(R.drawable.ic_notification_final)
            setAutoCancel(false)
            setColor(ContextCompat.getColor(applicationContext, R.color.new_product_blue))
            setContentIntent(pendingIntent)
            configure()
        }.build()

        notify(notificationId, notification)
    }

    @SuppressLint("MissingPermission")
    private fun notify(
        notificationId: Int,
        notification: Notification,
    ) {
        if (!hasNotificationPermission()) return

        NotificationManagerCompat.from(applicationContext)
            .notify(notificationId, notification)
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
