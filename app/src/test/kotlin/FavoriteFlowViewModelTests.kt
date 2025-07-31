import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.favorite.FavoriteFlowViewModel
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class FavoriteFlowViewModelTests {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var viewModel: FavoriteFlowViewModel
    private lateinit var cartManager: CartManager
    private lateinit var likeManager: LikeManager
    private lateinit var vodovozServiceRepository: VodovozServiceRepository

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
    }

    @Test
    fun `select category test`() = runTest {

    }

    @After
    fun tearDown() {

    }

}