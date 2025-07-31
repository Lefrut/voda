import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class CartManagerTests {

    private val dispatcher = StandardTestDispatcher()
    private var vodovozServiceRepository: VodovozServiceRepository =
        mockk<VodovozServiceRepository>()
    private var cartManager: CartManager = CartManager(vodovozServiceRepository, dispatcher)


    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun finished() {
        Dispatchers.resetMain()
    }

    @Test
    fun `multiple async cart changes with 5 success 2 fails`() = runTest {
        val addOperations = listOf<Triple<Flow<Result<String>>, Long, Int>>(
            Triple(flowOf(Result.success("Success")), 1, 2),
            Triple(flowOf(Result.success("Success")), 2, 2),
            Triple(flowOf(Result.success("Success")), 3, 2),
        )

        val updateOperations1 = listOf<Triple<Flow<Result<String>>, Long, Int>>(
            Triple(flow { throw RuntimeException("Update error") }, 2, 0),
            Triple(flowOf(Result.success("Success")), 3, 5),
            Triple(flow { throw RuntimeException("Update error") }, 3, 10),
        )

        val updateOperations2 = listOf(
            Triple(flowOf(Result.success("Success")), 1L, 0)
        )

        coEvery {
            vodovozServiceRepository.addProductToCart(any(), any())
        } returnsMany addOperations.map { it.first }

        coEvery {
            vodovozServiceRepository.updateProductInCart(any(), any())
        } returnsMany updateOperations1.map {
            it.first
        } + updateOperations2.map { it.first }


        val jobs = mutableListOf<Job>()

        addOperations.forEach { (_, id, count) ->
            jobs.add(async { cartManager.change(id, count).join() })
        }
        delay(2000)
        updateOperations1.forEach { (_, id, count) ->
            jobs.add(async { cartManager.change(id, count).join() })
        }
        delay(2000)
        updateOperations2.forEach { (_, id, count) ->
            jobs.add(async { cartManager.change(id, count).join() })
        }

        jobs.forEach { job -> job.join() }

        val cart = cartManager.observeCarts().firstOrNull() ?: emptyMap()

        assertEquals(cart.values.sum(), 4)
        assertEquals(cart.getOrDefault(1, 0), 0)
        assertEquals(cart[2], 2)
        assertEquals(cart[3], 2)
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
            assertEquals(cart?.toMap(), mapOf(productId to productCount))

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

    @Test
    fun `add products in empty cart by add()`() = runTest {
        coEvery {
            vodovozServiceRepository.addMultipleProductsToCart(any())
        } returns flow { emit(Result.success("")) }

        launch {
            cartManager.add(mapOf(1L to 10, 2L to 20))
        }.join()
        val cart = cartManager.observeCarts().firstOrNull() ?: emptyMap()
        assertEquals(
            mapOf(1L to 10, 2L to 20),
            cart
        )
    }

    @Test
    fun `add products in empty cart with error by add()`() = runTest {
        coEvery {
            vodovozServiceRepository.addMultipleProductsToCart(any())
        } returns flowOf(Result.failure(Throwable()))

        cartManager.add(mapOf(1L to 10, 2L to 20)).join()

        val cart = cartManager.observeCarts().firstOrNull() ?: emptyMap()

        assertEquals(
            emptyMap<Long, Int>(),
            cart.filter { it.value != 0 }
        )

        assert(cartManager.blockedProductsState.value.isEmpty())
    }

}