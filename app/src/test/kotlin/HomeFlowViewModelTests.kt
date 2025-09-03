import app.cash.turbine.test
import com.vodovoz.app.design_system.model.CategoryWithProductsUi
import com.vodovoz.app.design_system.model.SpecialPromotionUi
import com.vodovoz.app.design_system.model.StoryUi
import com.vodovoz.app.design_system.model.VodovozSectionUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.model.promotion.PopupWindowInfoModel
import com.vodovoz.app.domain.general.model.promotion.SpecialPromotionModel
import com.vodovoz.app.feature.home.HomeFlowViewModel
import com.vodovoz.app.feature.home.model.HomeListItem
import com.vodovoz.app.feature.home.model.compareVersions
import com.vodovoz.app.feature.home.model.getOrNull
import com.vodovoz.app.feature.home.model.getValueOrNull
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.spyk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeFlowViewModelTests : ViewModelTestBase<HomeFlowViewModel>() {

    override fun createViewModel(): HomeFlowViewModel {
        return spyk(
            HomeFlowViewModel(
                cartManager,
                likeManager,
                accountManager,
                vodovozServiceRepository,
                resourcesProvider,
                userPreferencesRepository
            )
        )
    }

    private val storiesUi = listOf(
        StoryUi(1, "", emptyList(), false),
        StoryUi(2, "", emptyList(), false),
        StoryUi(3, "", emptyList(), false),
        StoryUi(4, "", emptyList(), false)
    )

    private val topSection = HomeListItem.Products.topSection(
        VodovozSectionUi(
            title = "",
            items = listOf(CategoryWithProductsUi.Empty.copy(1)),
            button = null,
            placeholder = null
        )
    )
    private val bottomSection = HomeListItem.Products.bottomSection(
        VodovozSectionUi(
            title = "",
            items = listOf(CategoryWithProductsUi.Empty.copy(2)),
            button = null,
            placeholder = null
        )
    )

    private val homeItemsTest = listOf(
        bottomSection,
        topSection,
        HomeListItem.Stories(storiesUi)
    )


    @Test
    fun `get home item by type`() {
        val items = listOf(
            HomeListItem.Banner(emptyList()),
            HomeListItem.Stories(emptyList()),
            HomeListItem.Divider(444),
        )

        val storiesItem = items.getOrNull<HomeListItem.Stories>()
        val dividerItem = items.getOrNull<HomeListItem.Divider>()
        val stories = items.getValueOrNull() ?: storiesUi

        assertTrue(storiesItem != null)
        assertTrue(dividerItem != null)
        assertEquals(emptyList<StoryUi>(), stories)
    }

    @Test
    fun `listenStories when changed tests`() = runTest {

        viewModel.setState(HomeFlowViewModel.HomeState(items = homeItemsTest))

        coEvery { userPreferencesRepository.viewedStoryIds } returns flow {
            emit(emptyList())
            delay(200L)
            emit(listOf(1, 3))
            emit(listOf(3, 2))
        }

        val job = launch { viewModel.listenStories() }

        viewModel.state.onEach { println(it.stories) }.test {
            assertEquals(listOf<Long>(1, 2, 3, 4), awaitItem().stories.map { it.id })
            assertEquals(listOf<Long>(2, 4, 1, 3), awaitItem().stories.map { it.id })
            assertEquals(listOf<Long>(1, 4, 2, 3), awaitItem().stories.map { it.id })

            cancel()
        }

        job.cancel()
    }

    @Test
    fun `fetchHomeDetails tests`() = runTest {
        coEvery { vodovozServiceRepository.getBanners() } returnsMany listOf(
            flowOf(Result.success(emptyList())),
            flowOf(Result.failure(Throwable())),
        )

        viewModel.fetchHomeDetails().join()

        assertEquals(
            HomeFlowViewModel.HomeUiState.Success,
            viewModel.stateSnapshot.uiState
        )
        assertTrue(viewModel.stateSnapshot.items.size > 1)

        viewModel.fetchHomeDetails().join()

        assertEquals(
            HomeFlowViewModel.HomeUiState.NetworkError,
            viewModel.stateSnapshot.uiState
        )
    }

    @Test
    fun `refresh tests`() = runTest {
        every { viewModel.stateSnapshot } returns HomeFlowViewModel.HomeState(
            uiState = HomeFlowViewModel.HomeUiState.Success
        )
        every { viewModel.fetchHomeDetails(any()) } answers {
            val callback = arg<suspend (Int) -> Unit>(0)
            runBlocking { callback(0) }
            Job().apply { complete() }
        }

        launch {
            viewModel.state.test {
                assertEquals(false, awaitItem().showRefreshIndicator)
                assertEquals(true, awaitItem().showRefreshIndicator)
                assertEquals(false, awaitItem().showRefreshIndicator)
                cancelAndIgnoreRemainingEvents()
            }
        }


        viewModel.refresh().join()
    }


    @Test
    fun `selectCategory tests`() = runTest {
        viewModel.setState(
            HomeFlowViewModel.HomeState(items = homeItemsTest.shuffled())
        )

        viewModel.selectCategory(topSection, 1).join()

        val updatedTopSection =
            viewModel.stateSnapshot.items.firstOrNull {
                it.position == topSection.position
            } as HomeListItem.Products.CategoryWithProductsSection

        assertEquals(
            1, updatedTopSection.currentCategoryId
        )
        assertEquals(
            topSection.items, updatedTopSection.items
        )
        assertEquals(
            topSection.position, updatedTopSection.position
        )

    }

    @Test
    fun `goProfile tests`() = runTest {
        viewModel.events.test {
            viewModel.goToProfile().join()
            assertEquals(
                HomeFlowViewModel.HomeEvents.GoToProfile,
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `closeSpecialBs tests`() = runTest {
        viewModel.setState(HomeFlowViewModel.HomeState(showSpecialPromotionBS = true))

        viewModel.closeSpecialPromotionBottomSheet().join()

        assertEquals(false, viewModel.stateSnapshot.showSpecialPromotionBS)
    }

    @Test
    fun `fetchBottomSheets tests`() = runTest {
        every { vodovozServiceRepository.getPopupWindowInfo() } returnsMany listOf(
            flowOf(Result.success(PopupWindowInfoModel.Empty)),
            flowOf(Result.success(PopupWindowInfoModel.Empty))
        )
        mockkStatic(SpecialPromotionModel::toUi)
        mockkStatic(::compareVersions)

        every { any<SpecialPromotionModel>().toUi() } returns SpecialPromotionUi(
            1, "", "", "", null
        )

        every { compareVersions(any(), any()) } returnsMany listOf(1, -1)

        viewModel.fetchHomeDetails().join()

        assertEquals(true, viewModel.stateSnapshot.showSpecialPromotionBS)

        viewModel.fetchHomeDetails().join()

        assertEquals(false, viewModel.stateSnapshot.showSpecialPromotionBS)
        assertTrue(viewModel.stateSnapshot.uiState is HomeFlowViewModel.HomeUiState.AppNeedUpdate)


    }

    @Test
    fun `compareVersions tests`() = runTest {
        assertEquals(-1, compareVersions("1.0.0", "2.0.0"))
        assertEquals(0, compareVersions("0.0.0", "0.0.0"))
        assertEquals(0, compareVersions("2.0.0", "2.0.0"))
        assertEquals(1, compareVersions("0.0.1", "dwqqwdwq"))
        assertEquals(1, compareVersions("3.1.2", "3.1.1"))
        assertEquals(0, compareVersions("dwqqwd", "dwqqwdwq"))
    }


}