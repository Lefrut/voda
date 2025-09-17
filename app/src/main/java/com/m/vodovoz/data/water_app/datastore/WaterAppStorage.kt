package com.m.vodovoz.data.water_app.datastore

import kotlinx.coroutines.flow.Flow

interface WaterAppStorage {
    val stageFlow: Flow<String>
    val userInfoFlow: Flow<String>
    val notificationSettingsFlow: Flow<String>
    val dailyGoalFlow: Flow<String>

    suspend fun saveNotificationSettings(notificationSettings: String)
    suspend fun saveUserInfo(userInfo: String)
    suspend fun saveDailyGoal(dailyGoal: String)
    suspend fun saveStage(stage: String)

    suspend fun clearUserInfo()
    suspend fun clearDailyGoal()
    suspend fun clearNotificationSettings()
    suspend fun clearStage()

}