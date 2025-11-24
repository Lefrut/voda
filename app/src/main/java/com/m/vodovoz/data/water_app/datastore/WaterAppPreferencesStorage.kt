package com.m.vodovoz.data.water_app.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.Keep
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.core.content.edit

class WaterAppPreferencesStorage @Inject constructor(
    @ApplicationContext
    private val context: Context,
) : WaterAppStorage {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("water_app", Context.MODE_PRIVATE)
    }

    private val mutex = Mutex()

    @Keep
    private fun getDataFlow(key: String): Flow<String> = callbackFlow {
        val listener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
                if (changedKey == key) {
                    trySend(prefs.getString(key, "") ?: "")
                }
            }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(prefs.getString(key, "") ?: "")
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    private suspend fun editData(key: String, value: String) {
        mutex.withLock {
            prefs.edit { putString(key, value) }
        }
    }

    override val stageFlow: Flow<String>
        get() = getDataFlow(stageKey)

    override val userInfoFlow: Flow<String>
        get() = getDataFlow(userInfoKey)

    override val notificationSettingsFlow: Flow<String>
        get() = getDataFlow(notificationSettingsKey)

    override val dailyGoalFlow: Flow<String>
        get() = getDataFlow(dailyGoalKey)

    override suspend fun clearNotificationSettings() {
        editData(notificationSettingsKey, "")
    }

    override suspend fun clearStage() {
        editData(stageKey, "")
    }

    override suspend fun saveNotificationSettings(notificationSettings: String) {
        editData(notificationSettingsKey, notificationSettings)
    }

    override suspend fun saveUserInfo(userInfo: String) {
        editData(userInfoKey, userInfo)
    }

    override suspend fun saveDailyGoal(dailyGoal: String) {
        editData(dailyGoalKey, dailyGoal)
    }

    override suspend fun saveStage(stage: String) {
        editData(stageKey, stage)
    }

    override suspend fun clearUserInfo() {
        editData(userInfoKey, "")
    }

    override suspend fun clearDailyGoal() {
        editData(dailyGoalKey, "")
    }

    companion object {
        @Suppress("ConstPropertyName")
        private const val notificationSettingsKey = "NOTIFICATION_SETTINGS"
        @Suppress("ConstPropertyName")
        private const val userInfoKey = "USER_INFO"
        @Suppress("ConstPropertyName")
        private const val dailyGoalKey = "DAILY_GOAL"
        @Suppress("ConstPropertyName")
        private const val stageKey = "STAGE"
    }
}