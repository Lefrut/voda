package com.m.vodovoz.feature.profile.waterapp.worker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getColor
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.m.vodovoz.R
import com.m.vodovoz.common.notification.NotificationChannels
import com.m.vodovoz.common.notification.NotificationConfig
import com.m.vodovoz.domain.general.respository.WaterAppRepository
import com.m.vodovoz.feature.profile.waterapp.api.WaterAppNavKey
import com.m.vodovoz.ui.base.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalTime

@HiltWorker
class WaterAppWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val waterAppRepository: WaterAppRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data = Uri.Builder()
                .scheme("vodovoz")
                .authority("open")
                .appendPath(WaterAppNavKey.DEEP_LINK_PATH)
                .build()
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val iconColor = getColor(applicationContext, R.color.bluePrimary)

        val notificationSettings =
            waterAppRepository.getNotificationSettings().getOrNull() ?: return Result.success(Data.EMPTY)

        val currentMinutes = LocalTime.now()
        val wakeUpMinutes = notificationSettings.wakeUpTime
        val sleepMinutes = notificationSettings.sleepTime

        if (currentMinutes !in wakeUpMinutes..sleepMinutes) {
            return Result.success(Data.EMPTY)
        }

        val notification = NotificationCompat.Builder(
            applicationContext,
            NotificationChannels.NOTIFICATION_CHANNEL_ID
        )
            .setContentTitle(context.getString(R.string.time_to_drink_water))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(iconColor)
            .setColorized(true)
            .setSmallIcon(R.mipmap.notification_icon)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionGranted = ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (permissionGranted) {
                NotificationManagerCompat.from(applicationContext)
                    .notify(NotificationConfig.NOTIFICATION_ID, notification)
            }
        } else {
            NotificationManagerCompat.from(applicationContext)
                .notify(NotificationConfig.NOTIFICATION_ID, notification)
        }

        return Result.success(Data.EMPTY)
    }
}
