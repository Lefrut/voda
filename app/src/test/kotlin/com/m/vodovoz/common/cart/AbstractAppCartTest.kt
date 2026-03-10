@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

import com.m.vodovoz.common.cart.AbstractAppCart
import com.m.vodovoz.common.cart.AppCart
import com.m.vodovoz.common.cart.CartItem
import com.m.vodovoz.common.cart.CartRepository
import com.m.vodovoz.common.cart.OperationInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

/*
Assumptions (adjust to your project):
- CartItem has: id: Long, quantity: Int, isLoading: Boolean
- cart.requireItem(id) exists (or replace with cart.itemsState.value.getValue(id))
- CartRepository.execute(operationInfo) returns Result<Unit> or similar
- AbstractAppCart is your class under test
- OperationInfo, CartRequestType, etc. are internal. Repository can ignore them in fake.
*/


private class FakeCartRepository : CartRepository {
    var delayMs: Long = 0
    var result: Result<Unit> = Result.success(Unit)
    val executeCalls = AtomicInteger(0)

    override suspend fun execute(operationInfo: OperationInfo): Result<Unit> {
        executeCalls.incrementAndGet()
        if (delayMs > 0) delay(delayMs)
        return result
    }
}

private fun AppCart.requireItem(id: Long): CartItem = itemsState.value.getValue(id)

open class AbstractAppCartTest : CoroutineTestBase() {

    protected fun runCurrentTasks() = scheduler.runCurrent()
    protected fun advanceBy(ms: Long) = scheduler.advanceTimeBy(ms)

    private lateinit var repository: FakeCartRepository
    private lateinit var cart: AbstractAppCart

    @Before
    fun setUp() {
        repository = FakeCartRepository()
        cart = AbstractAppCart(
            cartRepository = repository,
            dispatcher = dispatcher
        )
    }

    @Test
    fun incrementProduct_shouldWaitForDebounce_whenStartPolicyDeferred() = runTest(scheduler) {
        repository.delayMs = 0
        cart.setStartPolicy(AppCart.StartPolicy.QUEUED)

        cart.incrementProduct(id = 7L, quantity = 1)
        cart.incrementProduct(id = 7L, quantity = 2)
        cart.incrementProduct(id = 7L, quantity = 3)

        runCurrentTasks()
        assertEquals(0, repository.executeCalls.get())

        advanceBy(374)
        runCurrentTasks()
        assertEquals(0, repository.executeCalls.get())

        advanceBy(1)
        runCurrentTasks()
        assertEquals(1, repository.executeCalls.get())
    }

    @Test
    fun incrementProduct_shouldExecuteImmediately_whenStartPolicyImmediate() = runTest(scheduler) {
        repository.delayMs = 0
        cart.setStartPolicy(AppCart.StartPolicy.IMMEDIATE)

        cart.incrementProduct(id = 7L, quantity = 5)
        runCurrentTasks()

        assertEquals(1, repository.executeCalls.get())
        assertEquals(5, cart.requireItem(7L).quantity)
        assertFalse(cart.requireItem(7L).isLoading)
    }



}