import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test

class CartManagerTests {

    private val vodovozServiceRepository = mockk<VodovozServiceRepository>()

    private val cartManager = CartManager(vodovozServiceRepository)
    private val dispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp(){
        Dispatchers.setMain(dispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun finished(){
        Dispatchers.resetMain()
    }


    @Test
    fun `adding first item in cart then check - version, cartMap, blockedProducts`() =
        runTest {
            val productId = 1L
            val productCount = 1

            coEvery {
                vodovozServiceRepository.addProductToCart(
                    productId,
                    productCount
                )
            } returns flow { emit(Result.success("Success")) }


            cartManager.change(productId, productCount).join()

            val cart = cartManager.observeCarts().firstOrNull()

            assert(cartManager.blockedProductsState.value.isEmpty())
            assert(cart != null)
            assert(cart?.toMap() == mapOf(productId to productCount))
            assert(cartManager.cartVersion != 0)

            coVerify {
                vodovozServiceRepository.addProductToCart(any(), any())
            }
        }


    @Test
    fun `change should update cart when input is valid`() = runTest {
        coEvery {
            vodovozServiceRepository.addProductToCart(any(), any())
        } returns flow {
            emit(Result.success("Success"))
        }

        cartManager.change(3, 1).join()

        assert(cartManager.observeUpdateCartList().value)
    }


}