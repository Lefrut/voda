package com.m.vodovoz.feature.profile.waterapp

import android.content.Context
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.m.vodovoz.common.water_app.NotificationSettings
import com.m.vodovoz.common.water_app.WaterApp
import com.m.vodovoz.feature.profile.waterapp.worker.WaterAppWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Stable
@Singleton
class WaterAppHelper @Inject constructor(
    @ApplicationContext
    private val context: Context,
) {

    data object Colors {
        val lightBlue = Color(0xFF5AC3FF)
        val darkBlue = Color(0xFF078FDD)
    }

    companion object {
        private const val TIME_FORMAT = "HH:mm"
        val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(TIME_FORMAT)
        private const val WATER_WORK_MANAGER_TAG = "water"
    }


    fun runOrCancelWorkManager(
        notificationSettings: NotificationSettings,
    ) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag(WATER_WORK_MANAGER_TAG)

        if (notificationSettings.enableNotifications) {
            val minutes = notificationSettings.notificationsDelay.inWholeMinutes

            val work = PeriodicWorkRequest.Builder(
                WaterAppWorker::class.java,
                minutes,
                TimeUnit.MINUTES
            )
                .setConstraints(Constraints.NONE)
                .setInitialDelay(minutes, TimeUnit.MINUTES)
                .addTag(WATER_WORK_MANAGER_TAG)
                .build()
            workManager.enqueueUniquePeriodicWork(
                WATER_WORK_MANAGER_TAG,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                work
            )

        }
    }


}