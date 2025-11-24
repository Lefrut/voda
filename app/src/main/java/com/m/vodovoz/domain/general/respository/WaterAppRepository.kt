package com.m.vodovoz.domain.general.respository

import com.m.vodovoz.common.water_app.NotificationSettings
import com.m.vodovoz.common.water_app.WaterApp
import kotlinx.coroutines.flow.Flow

interface WaterAppRepository {


    val stageFlow: Flow<Result<WaterApp.Stage>>
    val dailyGoalFlow: Flow<Result<WaterApp.DailyGoal>>
    val userInfoFlow: Flow<Result<WaterApp.UserInfo>>
    val settingsFlow: Flow<Result<NotificationSettings>>

    suspend fun saveNotificationSettings(
        notificationSettings: NotificationSettings,
    ): Result<NotificationSettings>

    suspend fun saveUserInfo(userInfo: WaterApp.UserInfo): Result<WaterApp.UserInfo>
    suspend fun saveDailyGoal(dailyGoal: WaterApp.DailyGoal): Result<WaterApp.DailyGoal>

    suspend fun clearNotificationSettings(): Result<Unit>
    suspend fun clearUserInfo(): Result<Unit>
    suspend fun clearDailyGoal(): Result<Unit>

    suspend fun getUserInfo(): Result<WaterApp.UserInfo>
    suspend fun getNotificationSettings(): Result<NotificationSettings>
    suspend fun getDailyGoal(): Result<WaterApp.DailyGoal>
    suspend fun saveStage(stage: WaterApp.Stage): Result<WaterApp.Stage>
    suspend fun clearStage(): Result<Unit>


    sealed class Exception : RuntimeException()

    class UnknownException(
        override val cause: Throwable? = null,
        override val message: String? = "FormatException",
    ) : Exception()

    class ParseException(
        override val cause: Throwable? = null,
        override val message: String? = "ParseException",
    ) : Exception()

}

suspend fun WaterAppRepository.clearAll() = runCatching {
    listOf(
        clearStage(),
        clearUserInfo(),
        clearNotificationSettings(),
        clearDailyGoal()
    ).forEach { result -> result.getOrThrow() }
}


fun Throwable.toUnknownException(): WaterAppRepository.UnknownException {
    return WaterAppRepository.UnknownException(cause, message)
}
