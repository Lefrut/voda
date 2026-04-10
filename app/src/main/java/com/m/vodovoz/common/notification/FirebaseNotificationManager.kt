package com.m.vodovoz.common.notification

import android.annotation.SuppressLint
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class FirebaseNotificationManager : FirebaseMessagingService() {

    private val notificationFactory by lazy { NotificationFactory(applicationContext) }

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
                notificationFactory.showLargeNotification(
                    image = imageUrl?.toString().orEmpty(),
                    title = title ?: "",
                    body = body ?: "",
                    data = jsonData
                )
            } else {
                notificationFactory.showSmallNotification(
                    title = title ?: "",
                    body = body ?: "",
                    data = jsonData
                )
            }
        }
    }
}
