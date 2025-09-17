package com.m.vodovoz.data.water_app.repository

import com.squareup.moshi.Moshi
import com.m.vodovoz.common.water_app.WaterApp
import com.m.vodovoz.core.network.serialization.fromJson
import com.m.vodovoz.core.network.serialization.toJson
import com.m.vodovoz.data.water_app.datastore.WaterAppStorage
import com.m.vodovoz.data.water_app.di.WaterAppMoshiQualifier
import com.m.vodovoz.domain.general.respository.WaterAppRepository
import com.m.vodovoz.domain.general.respository.toUnknownException
import com.m.vodovoz.util.extensions.firstResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class WaterAppRepositoryImpl @Inject constructor(
    private val storage: WaterAppStorage,
    @WaterAppMoshiQualifier
    protected val moshi: Moshi,
) : WaterAppRepository {

    override suspend fun getNotificationSettings(): Result<WaterApp.NotificationSettings> =
        settingsFlow.firstResult()

    override suspend fun getDailyGoal(): Result<WaterApp.DailyGoal> = dailyGoalFlow.firstResult()
    override suspend fun getUserInfo(): Result<WaterApp.UserInfo> = userInfoFlow.firstResult()


    override val userInfoFlow: Flow<Result<WaterApp.UserInfo>>
        get() = getFlowOperation(
            defaultValue = { WaterApp.UserInfo.ManDefault },
            flow = storage.userInfoFlow,
            onFormatError = ::clearUserInfo
        )


    override val settingsFlow: Flow<Result<WaterApp.NotificationSettings>>
        get() = getFlowOperation(
            defaultValue = { WaterApp.NotificationSettings.Default },
            flow = storage.notificationSettingsFlow,
            onFormatError = ::clearNotificationSettings
        )
    override val stageFlow: Flow<Result<WaterApp.Stage>>
        get() = getFlowOperation(
            defaultValue = { WaterApp.Stage("") },
            flow = storage.stageFlow,
            onFormatError = ::clearStage
        )

    override val dailyGoalFlow: Flow<Result<WaterApp.DailyGoal>>
        get() = getFlowOperation(
            defaultValue = {
                throw WaterAppRepository.UnknownException(
                    message = "Daily is not in storage"
                )
            },
            flow = storage.dailyGoalFlow,
            onFormatError = ::clearDailyGoal
        )


    override suspend fun saveNotificationSettings(
        notificationSettings: WaterApp.NotificationSettings,
    ): Result<WaterApp.NotificationSettings> = saveOperation(
        data = notificationSettings,
        block = storage::saveNotificationSettings
    )

    override suspend fun saveUserInfo(
        userInfo: WaterApp.UserInfo,
    ): Result<WaterApp.UserInfo> = saveOperation(
        data = userInfo,
        block = storage::saveUserInfo
    )

    override suspend fun saveDailyGoal(
        dailyGoal: WaterApp.DailyGoal,
    ): Result<WaterApp.DailyGoal> = saveOperation(
        data = dailyGoal,
        block = storage::saveDailyGoal
    )

    override suspend fun saveStage(
        stage: WaterApp.Stage,
    ): Result<WaterApp.Stage> = saveOperation(
        data = stage,
        block = storage::saveStage
    )


    override suspend fun clearStage(): Result<Unit> = clearOperation {
        storage.clearStage()
    }

    override suspend fun clearUserInfo(): Result<Unit> = clearOperation {
        storage.clearUserInfo()
    }

    override suspend fun clearDailyGoal(): Result<Unit> = clearOperation {
        storage.clearDailyGoal()
    }

    override suspend fun clearNotificationSettings(): Result<Unit> = clearOperation {
        storage.clearNotificationSettings()
    }

    private suspend fun clearOperation(block: suspend () -> Unit) = kotlin.runCatching {
        block()
    }

    private suspend inline fun <reified T : Any> saveOperation(
        data: T,
        noinline toJson: T.() -> String = { moshi.toJson(this) },
        noinline block: suspend (json: String) -> Unit,
    ): Result<T> = runCatching {
        block(data.toJson())
        data
    }.recoverCatching {
        throw it.toUnknownException()
    }


    private inline fun <reified R> getFlowOperation(
        noinline defaultValue: suspend () -> R,
        flow: Flow<String>,
        noinline onFormatError: suspend () -> Unit,
    ): Flow<Result<R>> {
        return flow.map { json ->
            if (json.isBlank()) return@map runCatching {
                defaultValue()
            }
            val data = fromJson<R>(json)
            data.recoverCatching { t ->
                if (t is WaterAppRepository.ParseException) {
                    onFormatError()
                    defaultValue()
                } else throw t.toUnknownException()
            }
        }.catch { t ->
            emit(
                Result.failure(t.toUnknownException())
            )
        }

    }

    private inline fun <reified R> fromJson(
        data: String,
        noinline fromJson: (String) -> R = {
            moshi.fromJson(it)
        },
    ): Result<R> = runCatching {
        fromJson(data)
    }.recoverCatching {
        Result.failure<R>(
            WaterAppRepository.ParseException(
                it.cause,
                it.message
            )
        ).getOrThrow()
    }


}
