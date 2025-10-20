package com.m.vodovoz.data.water_app.datastore

import android.content.Context
import androidx.annotation.Keep
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

private val Context.waterAppDataStore by preferencesDataStore(
    "water_app"
)


@Singleton
@Keep
class WaterAppDataStoreImpl @Inject constructor(
    @ApplicationContext
    private val context: Context,
) : WaterAppStorage {

    private val dataStore get() = context.waterAppDataStore

    @Keep
    private fun getDataFlow(key: Preferences.Key<String>) = dataStore.data.map {
        it[key] ?: ""
    }

    val mutex = Mutex()
    private suspend fun<T> editData(key: Preferences.Key<T>, value: T) = mutex.withLock {
        dataStore.edit { mutablePreferences ->
            mutablePreferences[key] = value
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
        editData(userInfoKey, "")
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
        editData(stageKey, "")
    }

    companion object {
        private val notificationSettingsKey = stringPreferencesKey("NOTIFICATION_SETTINGS_KEY")
        private val userInfoKey = stringPreferencesKey("USER_INFO_KEY")
        private val dailyGoalKey = stringPreferencesKey("DAILY_GOAL")
        private val stageKey = stringPreferencesKey("STAGE")
    }
}
