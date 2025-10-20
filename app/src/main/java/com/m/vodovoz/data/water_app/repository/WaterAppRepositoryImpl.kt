package com.m.vodovoz.data.water_app.repository

import androidx.annotation.Keep
import com.squareup.moshi.Moshi
import com.m.vodovoz.common.water_app.WaterApp
import com.m.vodovoz.core.network.serialization.fromJson
import com.m.vodovoz.core.network.serialization.toJson
import com.m.vodovoz.data.water_app.datastore.WaterAppStorage
import com.m.vodovoz.data.water_app.di.WaterAppMoshiQualifier
import com.m.vodovoz.domain.general.respository.WaterAppRepository
import com.m.vodovoz.domain.general.respository.toUnknownException
import com.m.vodovoz.feature.profile.waterapp.model.WaterAppUiState
import com.m.vodovoz.util.extensions.firstResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

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
            clazz = WaterApp.UserInfo::class,
            defaultValue = {
                WaterApp.UserInfo.ManDefault
            },
            flow = storage.userInfoFlow,
            onFormatError = ::clearUserInfo
        )


    override val settingsFlow: Flow<Result<WaterApp.NotificationSettings>>
        get() = getFlowOperation(
            clazz = WaterApp.NotificationSettings::class,
            defaultValue = {
                WaterApp.NotificationSettings.Default
            },
            flow = storage.notificationSettingsFlow,
            onFormatError = ::clearNotificationSettings
        )
    override val stageFlow: Flow<Result<WaterApp.Stage>>
        get() = getFlowOperation(
            clazz = WaterApp.Stage::class,
            defaultValue = { WaterApp.Stage("") },
            flow = storage.stageFlow,
            onFormatError = ::clearStage
        )

    override val dailyGoalFlow: Flow<Result<WaterApp.DailyGoal>>
        get() = getFlowOperation(
            clazz = WaterApp.DailyGoal::class,
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
        clazz = WaterApp.NotificationSettings::class,
        data = notificationSettings,
        block = storage::saveNotificationSettings
    )

    override suspend fun saveUserInfo(
        userInfo: WaterApp.UserInfo,
    ): Result<WaterApp.UserInfo> = saveOperation(
        clazz = WaterApp.UserInfo::class,
        data = userInfo,
        block = storage::saveUserInfo
    )

    override suspend fun saveDailyGoal(
        dailyGoal: WaterApp.DailyGoal,
    ): Result<WaterApp.DailyGoal> = saveOperation(
        clazz = WaterApp.DailyGoal::class,
        data = dailyGoal,
        block = storage::saveDailyGoal
    )

    override suspend fun saveStage(
        stage: WaterApp.Stage,
    ): Result<WaterApp.Stage> = saveOperation(
        clazz = WaterApp.Stage::class,
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

    private suspend fun <T : Any> saveOperation(
        clazz: KClass<T>,
        data: T,
        toJson: T.() -> String = { moshi.toJson(this, clazz.java) },
        block: suspend (json: String) -> Unit,
    ): Result<T> = runCatching {
        block(data.toJson())
        data
    }.recoverCatching {
        throw it.toUnknownException()
    }


    @Keep
    private fun <R : Any> getFlowOperation(
        clazz: KClass<R>,
        defaultValue: suspend () -> R,
        flow: Flow<String>,
        onFormatError: suspend () -> Unit,
    ): Flow<Result<R>> {
        return flow.map { json ->
            if (json.isBlank()) return@map runCatching {
                defaultValue()
            }
            val data = fromJson(clazz, json)
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

    @Keep
    private fun <R : Any> fromJson(
        clazz: KClass<R>,
        data: String,
        fromJson: (String) -> R = { json ->
            moshi.fromJson(json = json, type = clazz.java)
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
