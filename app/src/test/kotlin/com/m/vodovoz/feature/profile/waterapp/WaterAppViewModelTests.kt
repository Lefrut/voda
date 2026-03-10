package com.m.vodovoz.feature.profile.waterapp

import ViewModelTestBase
import androidx.datastore.core.IOException
import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import com.m.vodovoz.common.water_app.WaterApp
import com.m.vodovoz.domain.general.respository.WaterAppRepository
import com.m.vodovoz.feature.profile.waterapp.model.ReminderIntervalUi
import com.m.vodovoz.feature.profile.waterapp.model.WaterAppActivityLevelUi
import com.m.vodovoz.feature.profile.waterapp.model.WaterAppUiState
import com.m.vodovoz.feature.profile.waterapp.model.toStage
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

class WaterAppViewModelTests : ViewModelTestBase<WaterAppViewModel>() {

    private lateinit var waterAppRepository: WaterAppRepository
    private lateinit var waterAppHelper: WaterAppHelper

    @Before
    override fun setUpBase() {
        waterAppRepository = mockk(relaxed = true)
        waterAppHelper = mockk(relaxed = true)

        super.setUpBase()
    }

    override fun createViewModel(): WaterAppViewModel {
        return spyk(
            WaterAppViewModel(
                tabManager,
                insertsVisivilityState,
                waterAppRepository,
                waterAppHelper
            )
        )
    }

    private suspend fun initialStageTemplate(
        expectedUiState: WaterAppUiState,
        expectedCompleteSettings: Boolean,
        result: () -> WaterApp.Stage,
    ) {
        every { waterAppRepository.stageFlow } returns flowOf(kotlin.runCatching { result() })
        val viewModel2 = WaterAppViewModel(
            tabManager,
            insertsVisivilityState,
            waterAppRepository,
            waterAppHelper
        )

        viewModel2.state.test {
            awaitItem()
            val stateSnapshot = awaitItem()
            assertEquals(expectedUiState, stateSnapshot.uiState)
            assertEquals(expectedCompleteSettings, stateSnapshot.completeSettings)

            cancelAndIgnoreRemainingEvents()
        }

    }

    @Test
    fun `initial stage when success stage`() = runTest {
        initialStageTemplate(WaterAppUiState.Main, true) { WaterAppUiState.Main.toStage() }
    }

    @Test
    fun `initial stage when fail stage`() = runTest {
        initialStageTemplate(WaterAppUiState.Welcome, false) { throw IOException() }
    }


    @Test
    fun `select reminder interval`() = runTest {
        viewModel.selectReminderInterval(ReminderIntervalUi(100)).join()
        assertEquals(100.minutes, viewModel.stateSnapshot.notificationSettings.notificationsDelay)
    }

    @Test
    fun `select gender`() = runTest {
        mockkObject(WaterApp.Gender)
        every { WaterApp.Gender.from(any()) } returns WaterApp.Gender.Girl

        viewModel.selectGender(false).join()
        assertEquals(WaterApp.Gender.Girl, viewModel.stateSnapshot.userInfo.gender)
    }

    @Test
    fun `go to user stage`() = runTest {
        viewModel.state.test {
            viewModel.goToUserStage().join()
            assertEquals(WaterAppUiState.UserData.Gender, viewModel.stateSnapshot.uiState)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `navigate back`() = runTest {
        viewModel.events.test {
            viewModel.navigateBack().join()
            assertEquals(
                WaterAppViewModel.WaterAppEvents.GoBack,
                awaitItem()
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `select activity level`() = runTest {
        viewModel.selectActivityLevel(WaterAppActivityLevelUi.Low)

        suspend fun TurbineTestContext<WaterAppViewModel.WaterAppState>.awaitActivityLevel(): WaterApp.ActivityLevel {
            return awaitItem().userInfo.activityLevel
        }

        viewModel.state.test {
            assertEquals(WaterApp.ActivityLevel.Low, awaitActivityLevel())
            viewModel.selectActivityLevel(WaterAppActivityLevelUi.High)
            assertEquals(WaterApp.ActivityLevel.High, awaitActivityLevel())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `go to next user info stage`() = runTest {
        viewModel.goToNextStage(WaterAppUiState.UserData.Gender).join()

        viewModel.state.test {
            assertEquals(WaterAppUiState.UserData.Height, awaitItem().uiState)
            viewModel.goToNextStage(WaterAppUiState.UserData.ActivityLevel).join()
            assertEquals(WaterAppUiState.WaterGoal, awaitItem().uiState)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `go to previous user info stage`() = runTest {

        viewModel.goToPreviousStage(WaterAppUiState.UserData.Gender).join()

        viewModel.state.test {
            assertEquals(WaterAppUiState.Welcome, awaitItem().uiState)
            viewModel.goToPreviousStage(WaterAppUiState.UserData.SleepTime).join()
            assertEquals(WaterAppUiState.UserData.WakeUpTime, awaitItem().uiState)
        }
    }

    @Before
    override fun tearDownBase() {
        super.tearDownBase()
    }
}