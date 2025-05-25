package com.vodovoz.app.feature.profile.waterapp

import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.squareup.moshi.Moshi
import com.vodovoz.app.common.account.data.AccountManager
import com.vodovoz.app.common.datastore.DataStoreRepository
import com.vodovoz.app.feature.profile.waterapp.worker.WaterAppWorker
import com.vodovoz.app.util.extensions.debugLog
import com.vodovoz.app.util.extensions.fetchCurrentDayInTimeMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Stable
class WaterAppHelper @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val applicationContext: Application,
    private val accountManager: AccountManager,
    moshi: Moshi,
) {

    data object Colors{

        val lightBlue = Color(0xFF5AC3FF)
        val darkBlue = Color(0xFF078FDD)

    }

    companion object {
        const val WATER_APP_USER_DATA = "water app user data"
        const val WATER_APP_NOTIFICATION_DATA = "water app notification data"
        const val WATER_APP_RATE = "water app rate"

        val reminderIntervals = listOf(
            15L, 30L, 60L, 90L, 120L, 180L, 240L, 300L
        )

        @SuppressLint("DefaultLocale")
        val weights: List<Float> = (0..((300f - 20f) / 0.2f).toInt())
            .map { i ->
                val a = String.format(Locale("en"),"%.1f", 20f + i * 0.2f)
                a.toFloatOrNull() ?: 0f
            }

        val heights get() = (50..240).toList()

        val times = (0 until 1440 step 15).map { formatTime(it.toString()) }

        fun shouldDisplayIntervalAsHours(minutes: Long): Boolean = minutes > 91

        fun formatReminderMinutes(minutes: Long): String {
            return if (!shouldDisplayIntervalAsHours(minutes)) minutes.toString()
            else ((minutes / 60) + (minutes % 60).toFloat() / 60).toString()
        }

        fun formatTime(minutes: String): String {
            return try {
                val sleepTime = LocalTime.ofSecondOfDay(minutes.toLong() * 60)
                sleepTime.format(DateTimeFormatter.ofPattern(TIME_FORMAT))
            } catch (_: Throwable) {
                ""
            }
        }

        fun parseTime(time: String): String {
            return try {
                val minutes =
                    LocalTime.parse(time, DateTimeFormatter.ofPattern(TIME_FORMAT)).toSecondOfDay()
                        .toLong() / 60
                minutes.toString()
            } catch (_: Throwable) {
                ""
            }
        }


        private const val TIME_FORMAT = "HH:mm"

    }

    private val adapter = moshi.adapter(WaterAppUserData::class.java)
    private val adapterNotification = moshi.adapter(WaterAppNotificationData::class.java)
    private val adapterRate = moshi.adapter(WaterAppRateData::class.java)

    private val waterAppUserDataListener = MutableStateFlow<WaterAppUserData?>(null)
    fun observeWaterAppUserData() = waterAppUserDataListener.asStateFlow()

    private val waterAppNotificationDataListener = MutableStateFlow<WaterAppNotificationData?>(null)
    fun observeWaterAppNotificationData() = waterAppNotificationDataListener.asStateFlow()

    private val waterAppRateDataListener = MutableStateFlow<WaterAppRateData?>(null)
    fun observeWaterAppRateData() = waterAppRateDataListener.asStateFlow()

    fun fetchWaterAppUserData() {
        val json = dataStoreRepository.getString(WATER_APP_USER_DATA)

        val data = json?.takeIf { it.isNotEmpty() }?.let { adapter.fromJson(it) }

        waterAppUserDataListener.value = data ?: WaterAppUserData()
    }


    fun saveWaterAppUserData() {

        val data = waterAppUserDataListener.value

        val json = adapter.toJson(data)

        debugLog { "json $json" }

        dataStoreRepository.putString(WATER_APP_USER_DATA, json)
    }

    fun saveGender(gender: String) {
        waterAppUserDataListener.value = waterAppUserDataListener.value?.copy(
            gender = gender
        )
    }

    fun saveHeight(height: String) {
        waterAppUserDataListener.value = waterAppUserDataListener.value?.copy(
            height = height
        )
    }

    fun saveWeight(weight: String) {
        waterAppUserDataListener.value = waterAppUserDataListener.value?.copy(
            weight = weight
        )
    }

    fun saveSleepTime(sleepTime: String) {
        waterAppUserDataListener.value = waterAppUserDataListener.value?.copy(
            sleepTime = sleepTime
        )
    }

    fun saveWakeUpTime(wakeUpTime: String) {
        waterAppUserDataListener.value = waterAppUserDataListener.value?.copy(
            wakeUpTime = wakeUpTime
        )
    }

    fun saveSport(sport: String) {
        waterAppUserDataListener.value = waterAppUserDataListener.value?.copy(
            sport = sport
        )
    }

    fun saveStart(started: Boolean) {
        waterAppNotificationDataListener.value = waterAppNotificationDataListener.value?.copy(
            started = started
        )
    }

    fun saveRate(): Int {
        val rate = calculateRate()
        waterAppRateDataListener.value = waterAppRateDataListener.value?.copy(rate = rate)
        saveWaterAppRateData()
        return rate
    }

    fun fetchWaterAppRateData() {
        val currentDate = fetchCurrentDayInTimeMillis()
        val json = dataStoreRepository.getString(WATER_APP_RATE)

        val data = json?.takeIf { it.isNotEmpty() }?.let { adapterRate.fromJson(it) }

        val result = when {
            data == null -> WaterAppRateData(lastSavedDate = currentDate)
            data.lastSavedDate == 0L -> data.copy(lastSavedDate = currentDate)
            data.lastSavedDate != currentDate -> data.copy(lastSavedDate = currentDate, currentLevel = 0)
            else -> data
        }

        waterAppRateDataListener.value = result
    }

    fun startCalculate() {
        accountManager.reportEvent("trekervodi_vhod")
    }

    fun tryToChangeWaterLevel(levelChange: Int) {
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
        val json = adapterRate.toJson(data)
        dataStoreRepository.putString(WATER_APP_RATE, json)
    }

    private fun calculateRate(): Int {
        val weight = waterAppUserDataListener.value?.weight?.toDouble() ?: 50.0
        val sport = waterAppUserDataListener.value?.sport?.toDouble() ?: 0.25
        return ((1.5 + (weight - 20) * 0.02 + sport) * 1000).toInt()
    }

    fun saveNotificationFirstShow() {
        waterAppNotificationDataListener.value = waterAppNotificationDataListener.value?.copy(
            firstShow = true
        )
    }

    fun saveNotificationSwitch(switch: Boolean) {
        waterAppNotificationDataListener.value = waterAppNotificationDataListener.value?.copy(
            switch = switch
        )
    }

    fun saveNotificationTime(time: String) {
        waterAppNotificationDataListener.value = waterAppNotificationDataListener.value?.copy(
            time = time
        )
    }

    fun fetchWaterAppNotificationData() {
        val json = dataStoreRepository.getString(WATER_APP_NOTIFICATION_DATA)

        val data = if (!json.isNullOrEmpty()) {
            adapterNotification.fromJson(json)
        } else {
            null
        }

        waterAppNotificationDataListener.value = data ?: WaterAppNotificationData()
    }


    fun saveWaterAppNotificationData() {

        val data = waterAppNotificationDataListener.value ?: return
        val waterTag = "water"

        if (data.switch) {
            WorkManager.getInstance(applicationContext).cancelAllWorkByTag(waterTag)
            val work = PeriodicWorkRequest.Builder(
                WaterAppWorker::class.java,
                data.time.toLong(),
                TimeUnit.MINUTES
            )
                .setConstraints(Constraints.NONE)
                .setInitialDelay(data.time.toLong(), TimeUnit.MINUTES)
                .addTag(waterTag)
                .build()
            WorkManager
                .getInstance(applicationContext)
                .enqueueUniquePeriodicWork(waterTag, ExistingPeriodicWorkPolicy.UPDATE, work)
        } else {
            WorkManager
                .getInstance(applicationContext)
                .cancelAllWorkByTag(waterTag)
        }

        val json = adapterNotification.toJson(data)

        dataStoreRepository.putString(WATER_APP_NOTIFICATION_DATA, json)
    }

    fun fetchAppNotificationData(): WaterAppNotificationData {
        val json = dataStoreRepository.getString(WATER_APP_NOTIFICATION_DATA)
        debugLog { "json contains $json" }

        return json?.takeIf { it.isNotEmpty() }
            ?.let { adapterNotification.fromJson(it) }
            ?: WaterAppNotificationData()
    }


    fun clearData() {
        dataStoreRepository.remove(WATER_APP_NOTIFICATION_DATA)
        dataStoreRepository.remove(WATER_APP_USER_DATA)
        dataStoreRepository.remove(WATER_APP_RATE)

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
    )

}