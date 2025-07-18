import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkObject
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

    private val vodovozServiceRepository = mockk<VodovozServiceRepository>()

    private val dispatcher = StandardTestDispatcher()
    private val cartManager = CartManager(vodovozServiceRepository, dispatcher)

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

        assert(cart.values.sum() == 4)
        assert(cart.getOrDefault(1, 0) == 0)
        assert(cart[2] == 2)
        assert(cart[3] == 2)
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

    @Test
    fun `add products in empty cart by add()`() = runTest {
        coEvery {
            vodovozServiceRepository.addProductToCart(any(), any())
        } returns flow { Result.success("") }

        launch {
            cartManager.add(mapOf(1L to 10, 2L to 20))
        }.join()
        val cart = cartManager.observeCarts().firstOrNull() ?: emptyMap()

        assertEquals(
            cart,
            mapOf(1 to 10, 2 to 20)
        )
        assert(cartManager.blockedProductsState.value.isEmpty())
    }

    @Test
    fun `add products in empty cart with error by add()`() = runTest {
        launch {
            cartManager.add(mapOf(1L to 10, 2L to 20))
        }.join()
        val cart = cartManager.observeCarts().firstOrNull() ?: emptyMap()

        assertEquals(
            cart,
            mapOf(1 to 10, 2 to 20)
        )
        assert(cartManager.blockedProductsState.value.isEmpty())
    }

}