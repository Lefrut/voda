import androidx.collection.longListOf
import app.cash.turbine.test
import com.vodovoz.app.design_system.model.StoryUi
import com.vodovoz.app.design_system.model.mapToUi
import com.vodovoz.app.domain.general.model.promotion.StoryModel
import com.vodovoz.app.feature.home.HomeFlowViewModel
import com.vodovoz.app.feature.home.model.HomeListItem
import com.vodovoz.app.feature.home.model.getOrNull
import com.vodovoz.app.feature.home.model.getValueOrNull
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.spyk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

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

    private val storiesTestData = listOf(
        StoryUi(1, "", emptyList(), false),
        StoryUi(2, "", emptyList(), false),
        StoryUi(3, "", emptyList(), false),
        StoryUi(4, "", emptyList(), false)
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
        val stories = items.getValueOrNull() ?: storiesTestData

        assertTrue(storiesItem != null)
        assertTrue(dividerItem != null)
        assertEquals(emptyList<StoryUi>(), stories)
    }

    @Test
    fun `listenStories when changed tests`() = runTest {
        mockkStatic(List<StoryModel>::mapToUi)
        every { any<List<StoryModel>>().mapToUi() } returns storiesTestData
        coEvery { vodovozServiceRepository.getStories() } returns flow {
            emit(Result.success(emptyList()))
        }

        viewModel.fetchHomeDetails().join()

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


}