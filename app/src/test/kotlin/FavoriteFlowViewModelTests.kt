import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.favorite.FavoriteFlowViewModel
import com.vodovoz.app.feature.home.model.CategoryUi
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class FavoriteFlowViewModelTests {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var viewModel: FavoriteFlowViewModel
    private lateinit var cartManager: CartManager
    private lateinit var likeManager: LikeManager
    private lateinit var vodovozServiceRepository: VodovozServiceRepository

    private val viewState get() = viewModel.observeUiState().value.data

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        cartManager = mockk()
        likeManager = mockk()
        vodovozServiceRepository = mockk()
        viewModel = FavoriteFlowViewModel(
            cartManager,
            likeManager,
            vodovozServiceRepository
        )
        Dispatchers.setMain(dispatcher)
    }

    @Test
    fun `select another category`() = runTest {
        //viewModel.selectCategory(CategoryUi("name"))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

}