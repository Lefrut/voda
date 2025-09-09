import app.cash.turbine.test
import com.vodovoz.app.domain.general.respository.WaterAppRepository
import com.vodovoz.app.feature.profile.waterapp.WaterAppHelper
import com.vodovoz.app.feature.profile.waterapp.WaterAppViewModel
import com.vodovoz.app.feature.profile.waterapp.model.WaterAppUiState
import io.mockk.mockk
import io.mockk.spyk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

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
        return spyk(WaterAppViewModel(waterAppRepository, waterAppHelper))
    }

    /*
    * Стадии как элемент навигации.
    *   Стадии в соответвии с представлениями.
    *   Навигация взад, вперед.
    *   Сохранение чекпоинтов.
    *
    * Изменение параметров моделей WaterApp.
    *
    * */

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