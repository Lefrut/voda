package com.vodovoz.app.feature.profile.waterapp

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.squareup.moshi.Moshi
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.datastore.DataStorePrefs
import com.vodovoz.app.feature.profile.waterapp.WaterAppHelper.WaterAppNotificationData
import com.vodovoz.app.feature.profile.waterapp.WaterAppHelper.WaterAppRateData
import com.vodovoz.app.feature.profile.waterapp.WaterAppHelper.WaterAppUserData
import com.vodovoz.app.feature.profile.waterapp.worker.WaterAppWorker
import com.vodovoz.app.util.extensions.debugLog
import com.vodovoz.app.util.extensions.fetchCurrentDayInTimeMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Stable
@Singleton
class WaterAppHelper @Inject constructor(
    private val dataStorePrefs: DataStorePrefs,
    private val applicationContext: Application,
    private val accountManager: AccountManager,
    moshi: Moshi,
) {

    data object Colors {
        val lightBlue = Color(0xFF5AC3FF)
        val darkBlue = Color(0xFF078FDD)
    }

    companion object {
        const val WATER_APP_USER_DATA = "water app user data"
        const val WATER_APP_NOTIFICATION_DATA = "water app notification data"
        const val WATER_APP_RATE = "water app rate"

        val waterCupLevels = listOf(
            100, 250, 500, 750, 1000
        )

        val reminderIntervals = listOf(
            15L, 30L, 60L, 90L, 120L, 180L, 240L, 300L
        )

        val times = (0 until 1440 step 15).map { minutes ->
            try {
                val sleepTime = LocalTime.ofSecondOfDay(minutes.toLong() * 60)
                sleepTime.format(DateTimeFormatter.ofPattern(TIME_FORMAT))
            }catch (_: Throwable){
                ""
            }
        }

        fun shouldDisplayIntervalAsHours(minutes: Long): Boolean = minutes > 91

        fun formatReminderMinutes(minutes: Long): String {
            return if (!shouldDisplayIntervalAsHours(minutes)) minutes.toString()
            else ((minutes / 60) + (minutes % 60).toFloat() / 60).toString()
        }

        fun parseTime(time: String): String {
            return try {
                val minutes =
                    LocalTime.parse(time, DateTimeFormatter.ofPattern(TIME_FORMAT)).toSecondOfDay()
                        .toLong() / 60
                minutes.toString()
            } catch (_: Throwable) { "" }
        }


        private const val TIME_FORMAT = "HH:mm"

    }

    private val userDataJsonAdapter = moshi.adapter(WaterAppUserData::class.java)
    private val notificationJsonAdapter = moshi.adapter(WaterAppNotificationData::class.java)
    private val rateJsonAdapter = moshi.adapter(WaterAppRateData::class.java)

    private val waterAppUserDataListener = MutableStateFlow<WaterAppUserData?>(null)
    fun observeWaterAppUserData() = waterAppUserDataListener.asStateFlow()

    private val waterAppNotificationDataListener = MutableStateFlow<WaterAppNotificationData?>(null)
    fun observeWaterAppNotificationData() = waterAppNotificationDataListener.asStateFlow()

    private val waterAppRateDataListener = MutableStateFlow<WaterAppRateData?>(null)
    fun observeWaterAppRateData() = waterAppRateDataListener.asStateFlow()


    fun saveUserData() {
        val data = waterAppUserDataListener.value
        val json = userDataJsonAdapter.toJson(data)


        dataStorePrefs.putString(WATER_APP_USER_DATA, json)
    }

    fun setGender(gender: String) {
        waterAppUserDataListener.value = waterAppUserDataListener.value?.copy(
            gender = gender
        )
    }

    fun setHeight(height: String) {
        waterAppUserDataListener.update { userData ->
            userData?.copy(height = height)
        }
    }

    fun setWeight(weight: String) {
        waterAppUserDataListener.update { userData ->
            userData?.copy(weight = weight)
        }
    }

    fun setSleepTime(sleepTime: String) {
        waterAppUserDataListener.value = waterAppUserDataListener.value?.copy(
            sleepTime = sleepTime
        )
    }

    fun setWakeUpTime(wakeUpTime: String) {
        waterAppUserDataListener.value = waterAppUserDataListener.value?.copy(
            wakeUpTime = wakeUpTime
        )
    }

    fun setSport(sport: String) {
        waterAppUserDataListener.value = waterAppUserDataListener.value?.copy(
            sport = sport
        )
    }

    fun setStart(started: Boolean) {
        waterAppNotificationDataListener.value = waterAppNotificationDataListener.value?.copy(
            started = started
        )
    }

    fun calculateAndSaveRate(): Int {
        val rate = calculateRate()
        waterAppRateDataListener.value = waterAppRateDataListener.value?.copy(rate = rate)
        saveWaterAppRateData()
        return rate
    }

    fun tryToAddWater(levelChange: Int) {
        accountManager.reportEvent("trekervodi_chasha")

        val rateState = waterAppRateDataListener.value ?: return
        val current = rateState.currentLevel
        val max = rateState.rate

        val newLevel = (current + levelChange).coerceAtMost(max)
        val canFill = newLevel < max

        waterAppRateDataListener.value = rateState.copy(
            currentLevel = newLevel,
            canFill = canFill
        )
    }

    fun setWaterLevel(newLevel: Int) {
        val rateState = waterAppRateDataListener.value ?: return

        val max = rateState.rate

        val clampedLevel = newLevel.coerceIn(0, max)
        val canFill = clampedLevel < max

        waterAppRateDataListener.value = rateState.copy(
            currentLevel = clampedLevel,
            canFill = canFill
        )
    }

    fun saveWaterAppRateData() {
        val data = waterAppRateDataListener.value
        val json = rateJsonAdapter.toJson(data)
        dataStorePrefs.putString(WATER_APP_RATE, json)

        debugLog { "save water rate" }
    }

    private fun calculateRate(): Int {
        val weight = waterAppUserDataListener.value?.weight?.toDouble() ?: 50.0
        val sport = waterAppUserDataListener.value?.sport?.toDouble() ?: 0.25
        return ((1.5 + (weight - 20) * 0.02 + sport) * 1000).roundToInt()
    }

    fun setNotificationFirstShow() {
        waterAppNotificationDataListener.update { notificationData ->
            notificationData?.copy(firstShow = true)
        }
    }

    fun setNotificationSwitch(switch: Boolean) {
        waterAppNotificationDataListener.update { notificationData ->
            notificationData?.copy(switch = switch)
        }
    }

    fun setNotificationTime(time: String) {
        waterAppNotificationDataListener.update { notificationData ->
            notificationData?.copy(time = time)
        }
    }

    fun fetchWaterAppUserData() {
        val json = dataStorePrefs.getString(WATER_APP_USER_DATA)
        val data = json?.takeIf { it.isNotEmpty() }?.let { userDataJsonAdapter.fromJson(it) }
        waterAppUserDataListener.value = data ?: WaterAppUserData()
    }


    fun fetchWaterAppRateData() {
        val currentDate = fetchCurrentDayInTimeMillis()
        val json = dataStorePrefs.getString(WATER_APP_RATE)

        val data = json?.takeIf { it.isNotEmpty() }?.let { rateJsonAdapter.fromJson(it) }

        val result = when {
            data == null -> WaterAppRateData(lastSavedDate = currentDate, canFill = true)
            data.lastSavedDate == 0L -> data.copy(lastSavedDate = currentDate)
            data.lastSavedDate != currentDate -> data.copy(
                lastSavedDate = currentDate,
                currentLevel = 0,
                canFill = true
            )

            else -> data
        }

        waterAppRateDataListener.value = result
    }


    fun fetchWaterAppNotificationData() {
        val json = dataStorePrefs.getString(WATER_APP_NOTIFICATION_DATA)

        val data = if (!json.isNullOrEmpty()) {
            notificationJsonAdapter.fromJson(json)
        } else {
            null
        }

        waterAppNotificationDataListener.value = data ?: WaterAppNotificationData()
    }


    fun saveWaterAppNotificationData() {

        val data = waterAppNotificationDataListener.value ?: return
        val waterTag = "water"

        val workManager = WorkManager.getInstance(applicationContext)

        workManager.cancelAllWorkByTag(waterTag)
        if (data.switch) {
            val minutes = data.time.toLong()

            val work = PeriodicWorkRequest.Builder(
                WaterAppWorker::class.java,
                minutes,
                TimeUnit.MINUTES
            )
                .setConstraints(Constraints.NONE)
                .setInitialDelay(minutes, TimeUnit.MINUTES)
                .addTag(waterTag)
                .build()
            workManager.enqueueUniquePeriodicWork(
                waterTag,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                work
            )
        }

        val notificationJson = notificationJsonAdapter.toJson(data)

        dataStorePrefs.putString(WATER_APP_NOTIFICATION_DATA, notificationJson)
    }

    fun clearData() {
        dataStorePrefs.remove(WATER_APP_NOTIFICATION_DATA)
        dataStorePrefs.remove(WATER_APP_USER_DATA)
        dataStorePrefs.remove(WATER_APP_RATE)

        fetchWaterAppUserData()
        fetchWaterAppNotificationData()
        fetchWaterAppRateData()
    }

    @Immutable
    data class WaterAppNotificationData(
        val firstShow: Boolean = false,
        val switch: Boolean = false,
        val time: String = reminderIntervals.getOrElse(2) { 90 }.toString(),
        val started: Boolean = false,
    )


    @Immutable
    data class WaterAppUserData(
        val gender: String = "man",
        val height: String = "170",
        val weight: String = "70.0",
        val sleepTime: String = "1320",
        val wakeUpTime: String = "420",
        val sport: String = "0.25",
    ) {

        fun formatSleepTime(): String {
            return try {
                val sleepTime = LocalTime.ofSecondOfDay(sleepTime.toLong() * 60)
                sleepTime.format(DateTimeFormatter.ofPattern(TIME_FORMAT))
            } catch (_: Throwable) {
                ""
            }
        }

        fun formatWakeUpTime(): String {
            return try {
                val wakeUpTime = LocalTime.ofSecondOfDay(wakeUpTime.toLong() * 60)
                wakeUpTime.format(DateTimeFormatter.ofPattern(TIME_FORMAT))
            } catch (_: Throwable) {
                ""
            }
        }

        companion object {
            private const val TIME_FORMAT = "HH:mm"
        }


    }

    @Immutable
    data class WaterAppRateData(
        val rate: Int = 2300,
        val currentLevel: Int = 0,
        val lastSavedDate: Long = 0,
        val canFill: Boolean = true,
        val wasCompleted: Boolean = false
    )

}