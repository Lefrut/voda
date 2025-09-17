package com.m.vodovoz.data.water_app.repository

import CoroutineTestBase
import android.content.Context
import androidx.datastore.core.IOException
import app.cash.turbine.test
import com.squareup.moshi.Moshi
import com.m.vodovoz.common.water_app.WaterApp
import com.m.vodovoz.core.network.serialization.toJson
import com.m.vodovoz.data.water_app.datastore.WaterAppDataStoreImpl
import com.m.vodovoz.data.water_app.datastore.WaterAppStorage
import com.m.vodovoz.data.water_app.di.WaterAppDataModule
import com.m.vodovoz.domain.general.respository.WaterAppRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import java.time.LocalTime
import kotlin.reflect.KClass


class WaterAppRepositoryTests: CoroutineTestBase() {


    @Mock
    private lateinit var ctx: Context
    private lateinit var waterAppRepository: WaterAppRepository
    private lateinit var waterAppStorage: WaterAppStorage
    private lateinit var moshi: Moshi

    private val defaultNotificationSettings = WaterApp.NotificationSettings.Default

    private val notificationSettings = defaultNotificationSettings.copy(
        enableNotifications = true,
        wakeUpTime = LocalTime.of(11, 0)
    )
    private val storageIoException = IOException()
    private val man = WaterApp.UserInfo.ManDefault
    private val girl = WaterApp.UserInfo.GirlDefault

    @Before
    override fun setUpBase() {
        super.setUpBase()


        ctx =  mockk()
        moshi = spyk(WaterAppDataModule.provideMoshi())
        waterAppStorage = spyk(
            WaterAppDataStoreImpl(ctx)
        )
        waterAppRepository = spyk(
            WaterAppRepositoryImpl(
                waterAppStorage,
                moshi
            )
        )
    }

    @Test
    fun `save stage`() = runTest {
        val stage = WaterApp.Stage("1234512321")
        coEvery { waterAppStorage.saveStage(any()) } returns Unit
        assertEquals(
            Result.success(stage.name),
            waterAppRepository.saveStage(stage)
        )
    }

    @Test
    fun `clear stage`() = runTest {
        coEvery { waterAppStorage.clearStage() } returns Unit

        assertEquals(Result.success(Unit), waterAppRepository.clearStage())
    }

    @Test
    fun `get stage flow`() = runTest {
        val stage = WaterApp.Stage("zzzzzzzzz")

        coEvery { waterAppStorage.stageFlow } returns flow {
            emit(stage.name)
        }

        waterAppRepository.stageFlow.test {
            assertEquals(stage.name, awaitItem().getOrThrow().name)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save daily goal`() = runTest {
        val dailyGoal = WaterApp.DailyGoal.create(1850)
        coEvery { waterAppStorage.saveDailyGoal(any()) } returns Unit

        assertEquals(Result.success(dailyGoal), waterAppRepository.saveDailyGoal(dailyGoal))
    }

    @Test
    fun `get daily goal flow`() = runTest {
        val dailyGoal = spyk(WaterApp.DailyGoal.create(2550))

        every { dailyGoal.plusMl(any()) } returnsMany listOf(
            dailyGoal.copy(currentMl = 500),
            dailyGoal.copy(currentMl = 800)
        )

        val dailyGoalAfterDrink = dailyGoal.plusMl(500)

        coEvery {
            waterAppStorage.dailyGoalFlow
        } returns flow {
            emit(moshi.toJson(dailyGoal))
            emit(moshi.toJson(dailyGoalAfterDrink))
            emit("12321321")
        }
        coEvery { waterAppRepository.clearDailyGoal() } returns Result.success(Unit)
        coEvery { waterAppRepository.userInfoFlow } returns flow {
            emit(Result.success(man))
        }

        mockkObject(WaterApp)
        every { WaterApp.calculateDailyGoal(any()) } returns dailyGoal

        waterAppRepository.dailyGoalFlow.test {
            assertEquals(Result.success(dailyGoal), awaitItem())
            assertEquals(Result.success(dailyGoalAfterDrink), awaitItem())
            assertEquals(true, awaitItem().isFailure)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clear daily goal`() = runTest {
        coEvery { waterAppStorage.clearDailyGoal() } returns Unit

        assertEquals(Result.success(Unit), waterAppRepository.clearDailyGoal())
    }

    @Test
    fun `get daily goal`() = runTest {
        val dailyGoal = WaterApp.DailyGoal.create(3000)

        every {
            waterAppRepository.dailyGoalFlow
        } returns flow {
            emit(Result.success(dailyGoal))
        }

        assertEquals(
            Result.success(dailyGoal),
            waterAppRepository.getDailyGoal()
        )
    }



    @Test
    fun `get initial notification settings`() = runTest {
        coEvery { waterAppRepository.settingsFlow } returns flow {
            emit(Result.success(defaultNotificationSettings))
        }

        assertEquals(
            Result.success(defaultNotificationSettings),
            waterAppRepository.getNotificationSettings()
        )

    }

    @Test
    fun `save notification settings when storage success`() = runTest {
        coEvery { waterAppStorage.saveNotificationSettings(any()) } returns Unit

        assertEquals(
            Result.success(notificationSettings),
            waterAppRepository.saveNotificationSettings(notificationSettings)
        )
    }

    @Test
    fun `fail save notification settings when storage error`() = runTest {
        coEvery { waterAppStorage.saveNotificationSettings(any()) } answers {
            throw storageIoException
        }

        assertEquals(
            WaterAppRepository.UnknownException::class.java,
            waterAppRepository.saveNotificationSettings(notificationSettings)
                .exceptionOrNull()?.javaClass
        )
    }

    @Test
    fun `clear notification settings`() = runTest {
        coEvery { waterAppStorage.clearNotificationSettings() } coAnswers { }
        assertEquals(Result.success(Unit), waterAppRepository.clearNotificationSettings())
        coEvery { waterAppStorage.clearNotificationSettings() } coAnswers { throw storageIoException }
        assertEquals(
            Result.failure<Unit>(storageIoException),
            waterAppRepository.clearNotificationSettings()
        )
    }

    @Test
    fun `notification settings flow`() = runTest {
        val settingsJson = moshi.toJson(notificationSettings)
        every { waterAppStorage.notificationSettingsFlow } returns flow {
            emit("")
            emit("123456")
            emit(settingsJson)
            throw storageIoException
        }

        coEvery {
            waterAppRepository.clearNotificationSettings()
        } returnsMany listOf(
            Result.success(Unit),
            Result.failure(storageIoException)
        )

        waterAppRepository.settingsFlow.test {
            assertEquals(
                Result.success(defaultNotificationSettings),
                awaitItem()
            )
            assertEquals(
                Result.success(defaultNotificationSettings),
                awaitItem()
            )
            assertEquals(
                Result.success(notificationSettings),
                awaitItem()
            )

            assertEquals(
                WaterAppRepository.UnknownException::class.java,
                awaitItem().exceptionOrNull()?.javaClass
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save user info`() = runTest {
        coEvery { waterAppStorage.saveUserInfo(any()) } returns Unit

        assertEquals(Result.success(girl), waterAppRepository.saveUserInfo(girl))
    }

    @Test
    fun `get user info`() = runTest {
        every { waterAppRepository.userInfoFlow } returnsMany listOf(
            flow { emit(Result.success(man)) },
            flow {}
        )
        assertEquals(Result.success(man), waterAppRepository.getUserInfo())
        assertEquals(true, waterAppRepository.getUserInfo().isFailure)
    }

    @Test
    fun `user info flow from storage`() = runTest {

        val lastUserInfo = man.copy(
            activityLevel = WaterApp.ActivityLevel.High
        )

        coEvery { waterAppRepository.clearUserInfo() } returns Result.success(Unit)

        every { waterAppStorage.userInfoFlow } returns flow {
            emit("")
            emit(moshi.toJson(girl))
            emit("12312lpofwop")
            emit(moshi.toJson(lastUserInfo))
        }

        waterAppRepository.userInfoFlow.test {
            assertEquals(
                Result.success(man),
                awaitItem()
            )
            assertEquals(
                Result.success(girl),
                awaitItem()
            )
            assertEquals(
                Result.success(man),
                awaitItem()
            )
            assertEquals(
                Result.success(lastUserInfo),
                awaitItem()
            )

            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) {
            waterAppRepository.clearUserInfo()
        }
    }

    @Test
    fun `erasure types class check`() = runTest {
        fun <T : Any> method(value: List<T>): KClass<out T> {
            return value.first()::class
        }

        assertEquals(1::class, method(listOf(2)))

    }

}
