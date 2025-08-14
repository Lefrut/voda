import app.cash.turbine.test
import com.vodovoz.app.domain.general.model.product.ProductsSectionModel
import com.vodovoz.app.feature.favorite.FavoriteFlowViewModel
import com.vodovoz.app.feature.home.model.CategoryUi
import com.vodovoz.app.feature.product_comments.model.SortUi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FavoriteFlowViewModelTests : ViewModelTestBase<FavoriteFlowViewModel>() {


    private val viewState get() = viewModel.state.value

    override fun createViewModel(): FavoriteFlowViewModel {
        return spyk(
            FavoriteFlowViewModel(
                cartManager,
                likeManager,
                vodovozServiceRepository
            )
        )
    }

    @Test
    fun `fetch favorites test - success`() = runTest {
        coEvery {
            vodovozServiceRepository.getFavoriteProducts(any())
        } returns flowOf(
            Result.success(ProductsSectionModel.Empty)
        )

        launch {
            viewModel.state.map { pagingState ->
                pagingState
            }.test {
                assertEquals(FavoriteFlowViewModel.FavoriteUiState.Loading, awaitItem().uiState)
                val stateAfterSuccess = awaitItem()
                assertEquals(FavoriteFlowViewModel.FavoriteUiState.Success, stateAfterSuccess.uiState)
                cancelAndIgnoreRemainingEvents()
            }
        }

        viewModel.fetchFavoriteProducts().join()
    }

    @Test
    fun `fetch favorites test - error`() = runTest {
        coEvery {
            vodovozServiceRepository.getFavoriteProducts(any())
        } returns flowOf(Result.failure(RuntimeException()))

        launch {
            viewModel.state.map { pagingState ->
                pagingState
            }.test {
                assertEquals(FavoriteFlowViewModel.FavoriteUiState.Loading, awaitItem().uiState)
                val stateAfterError = awaitItem()
                assertEquals(FavoriteFlowViewModel.FavoriteUiState.Error, stateAfterError.uiState)
                cancelAndIgnoreRemainingEvents()
            }
        }

        viewModel.fetchFavoriteProducts().join()
    }


    @Test
    fun `select category`() = runTest {
        coEvery {
            viewModel.fetchFavoriteProducts()
        } returns mockk(relaxed = true)

        val category = CategoryUi("name", 0)

        viewModel.selectCategory(category).join()

        assertEquals(category, viewState.currentCategory)

        coVerify { viewModel.fetchFavoriteProducts() }
    }

    @Test
    fun `select sort`() = runTest {
        coEvery {
            viewModel.fetchFavoriteProducts()
        } returns mockk(relaxed = true)

        val sort = SortUi("All", "all", "asc")

        viewModel.selectSort(sort).join()

        assertEquals(sort, viewState.currentSort)

        coVerify { viewModel.fetchFavoriteProducts() }
    }

    @Test
    fun `test has favorite changes when no changes, changes and extreme cases`() = runTest {
        coEvery {
            viewModel.fetchFavoriteProducts()
        } returns mockk(relaxed = true)

        val lastSavedLikesList = listOf(
            mapOf(),
            mapOf(1L to true),
            mapOf(),
            mapOf(1L to true, 2L to true, 3L to false),
            mapOf(1L to true, 2L to true, 3L to true),
        )

        coEvery {
            viewModel.stateSnapshot.lastSavedLikes
        } returnsMany lastSavedLikesList
        coEvery {
            likeManager.getLikes()
        } returnsMany listOf(
            mapOf(),
            mapOf(),
            mapOf(1L to true),
            mapOf(1L to true),
            mapOf(1L to true, 2L to true, 3L to true),
        )

        repeat(lastSavedLikesList.size) { viewModel.fetchFavoritesIfChanges().join() }

        coVerify(exactly = 3) { viewModel.fetchFavoriteProducts() }
        coVerify(exactly = lastSavedLikesList.size) { viewModel.fetchFavoritesIfChanges() }
    }


}