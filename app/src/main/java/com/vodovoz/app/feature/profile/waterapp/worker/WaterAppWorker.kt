package com.vodovoz.app.feature.profile.waterapp.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.hilt.work.HiltWorker
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.vodovoz.app.R
import com.vodovoz.app.common.notification.NotificationChannels
import com.vodovoz.app.common.notification.NotificationConfig
import com.vodovoz.app.feature.profile.waterapp.WaterAppHelper
import com.vodovoz.app.util.extensions.debugLog
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalTime
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAdjusters
import java.time.temporal.TemporalField
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

@HiltWorker
class WaterAppWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val waterAppHelper: WaterAppHelper,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        debugLog { "WaterAppWorker: doWork() started" }

        val pendingIntent = NavDeepLinkBuilder(applicationContext)
            .setGraph(R.navigation.nav_graph_profile)
            .setDestination(R.id.waterAppFragment)
            .createPendingIntent()

        val iconColor = getColor(applicationContext, R.color.bluePrimary)
        debugLog { "WaterAppWorker: PendingIntent и iconColor инициализированы" }

        waterAppHelper.fetchWaterAppUserData()
        debugLog { "WaterAppWorker: вызван fetchWaterAppUserData()" }

        val userData = waterAppHelper.observeWaterAppUserData().value
        if (userData == null) {
            debugLog { "WaterAppWorker: userData == null, выходим с Result.success(Data.EMPTY)" }
            return Result.success(Data.EMPTY)
        }

        debugLog { "WaterAppWorker: userData получены — wakeUpTime=${userData.wakeUpTime}, sleepTime=${userData.sleepTime}" }

        val currentMinutes = LocalTime.now().get(ChronoField.MINUTE_OF_DAY)
        val wakeUpMinutes = userData.wakeUpTime.toIntOrNull() ?: 0
        val sleepMinutes = userData.sleepTime.toIntOrNull() ?: 1000

        debugLog { "WaterAppWorker: currentMinutes=$currentMinutes, wakeUpMinutes=$wakeUpMinutes, sleepMinutes=$sleepMinutes" }

        if (currentMinutes !in wakeUpMinutes..sleepMinutes) {
            debugLog { "WaterAppWorker: Вне временного интервала, уведомление не отправляется" }
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

        debugLog { "WaterAppWorker: уведомление собрано, проверка разрешений (если требуется)" }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionGranted = ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            debugLog { "WaterAppWorker: POST_NOTIFICATIONS permission granted = $permissionGranted" }

            if (permissionGranted) {
                NotificationManagerCompat.from(applicationContext)
                    .notify(NotificationConfig.NOTIFICATION_ID, notification)
                debugLog { "WaterAppWorker: уведомление отправлено (TIRAMISU+)" }
            } else {
                debugLog { "WaterAppWorker: разрешение на уведомления не получено, уведомление не отправлено" }
            }
        } else {
            NotificationManagerCompat.from(applicationContext)
                .notify(NotificationConfig.NOTIFICATION_ID, notification)
            debugLog { "WaterAppWorker: уведомление отправлено (до TIRAMISU)" }
        }

        debugLog { "WaterAppWorker: doWork() завершён" }
        return Result.success(Data.EMPTY)
    }}