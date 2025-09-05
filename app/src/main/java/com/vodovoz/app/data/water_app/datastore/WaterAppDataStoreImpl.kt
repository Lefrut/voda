package com.vodovoz.app.data.water_app.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.vodovoz.app.core.datastore.get
import com.vodovoz.app.core.datastore.set
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.waterAppDataStore by preferencesDataStore(
    "water_app.pb"
)


@Singleton
class WaterAppDataStoreImpl @Inject constructor(
    @ApplicationContext
    private val context: Context,
) : WaterAppStorage {

    private val dataStore get() = context.waterAppDataStore

    private fun getDataFlow(key: String) = dataStore.data.map {
        it[key] ?: ""
    }

    private suspend fun editData(key: String, value: String) {
        dataStore.edit { mutablePreferences ->
            mutablePreferences[key] = value
        }
    }

    override val stageFlow: Flow<String>
        get() = getDataFlow(STAGE)

    override val userInfoFlow: Flow<String>
        get() = getDataFlow(USER_INFO_KEY)

    override val notificationSettingsFlow: Flow<String>
        get() = getDataFlow(NOTIFICATION_SETTINGS_KEY)
    override val dailyGoalFlow: Flow<String>
        get() = getDataFlow(DAILY_GOAL)


    override suspend fun clearNotificationSettings() {
        editData(NOTIFICATION_SETTINGS_KEY, "")
    }

    override suspend fun clearStage() {
        editData(STAGE, "")
    }

    override suspend fun saveNotificationSettings(notificationSettings: String) {
        editData(NOTIFICATION_SETTINGS_KEY, notificationSettings)
    }

    override suspend fun saveUserInfo(userInfo: String) {
        editData(USER_INFO_KEY, userInfo)
    }

    override suspend fun saveDailyGoal(dailyGoal: String) {
        editData(DAILY_GOAL, dailyGoal)
    }

    override suspend fun saveStage(stage: String) {
        editData(STAGE, stage)
    }

    override suspend fun clearUserInfo() {
        editData(USER_INFO_KEY, "")
    }

    override suspend fun clearDailyGoal() {
        editData(DAILY_GOAL, "")
    }

    companion object {
        private const val NOTIFICATION_SETTINGS_KEY = "NOTIFICATION_SETTINGS_KEY"
        private const val USER_INFO_KEY = "USER_INFO_KEY"
        private const val DAILY_GOAL = "DAILY_GOAL"
        private const val STAGE = "STAGE"
    }
}
