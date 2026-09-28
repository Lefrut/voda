package com.m.vodovoz.common.cart

import com.m.vodovoz.domain.general.respository.CartRepository
import com.m.vodovoz.domain.general.respository.CartRequest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppCartManagerAwaitTests {
    @Test
    fun `await returns applied and no change`() = runTest {
        val requests = mutableListOf<CartRequest>()
        val manager = AppCartManager(
            parentScope = this,
            repository = repository { request ->
                requests += request
                Result.success(Unit)
            },
            nowMillis = { testScheduler.currentTime },
        )
        try {
            manager.loadCart(emptyMap())
            val added = async { manager.setQuantityAwait(1, 2, DispatchMode.IMMEDIATE) }
            runCurrent()
            assertEquals(CartItemChange.Applied(0, 2), added.await())

            val unchanged = async { manager.setQuantityAwait(1, 2, DispatchMode.IMMEDIATE) }
            runCurrent()
            assertEquals(CartItemChange.NoChange(2), unchanged.await())
            assertEquals(listOf(CartRequest.AddProduct(1, 2)), requests)
        } finally {
            manager.close()
        }
    }

    @Test
    fun `debounced command completes when replaced`() = runTest {
        val requests = mutableListOf<CartRequest>()
        val manager = AppCartManager(
            parentScope = this,
            repository = repository { request ->
                requests += request
                Result.success(Unit)
            },
            nowMillis = { testScheduler.currentTime },
        )
        try {
            manager.loadCart(emptyMap())
            val first = async { manager.setQuantityAwait(1, 1) }
            runCurrent()
            val second = async { manager.setQuantityAwait(1, 2) }
            runCurrent()

            assertEquals(CartItemChange.Superseded, first.await())
            advanceTimeBy(300)
            runCurrent()
            assertEquals(CartItemChange.Applied(0, 2), second.await())
            assertEquals(listOf(CartRequest.AddProduct(1, 2)), requests)
        } finally {
            manager.close()
        }
    }

    @Test
    fun `bulk command waits for all requests and reports each outcome`() = runTest {
        val update = CompletableDeferred<Result<Unit>>()
        val manager = AppCartManager(
            parentScope = this,
            repository = repository { request ->
                if (request is CartRequest.UpdateProduct) update.await()
                else Result.success(Unit)
            },
            nowMillis = { testScheduler.currentTime },
        )
        try {
            manager.loadCart(mapOf(1L to 2))
            val result = async {
                manager.setQuantitiesAwait(mapOf(1L to 3, 2L to 1), DispatchMode.IMMEDIATE)
            }
            runCurrent()
            assertFalse(result.isCompleted)

            update.complete(Result.failure(IllegalStateException("request failed")))
            runCurrent()
            val items = result.await().items
            assertEquals(CartItemChange.Applied(0, 1), items[2L])
            assertEquals("request failed", (items[1L] as CartItemChange.Failed).error.message)
        } finally {
            manager.close()
        }
    }

    @Test
    fun `reload releases command waiting for server`() = runTest {
        val response = CompletableDeferred<Result<Unit>>()
        val manager = AppCartManager(
            parentScope = this,
            repository = repository { response.await() },
            nowMillis = { testScheduler.currentTime },
        )
        try {
            manager.loadCart(emptyMap())
            val result = async { manager.setQuantityAwait(1, 1, DispatchMode.IMMEDIATE) }
            runCurrent()
            assertFalse(result.isCompleted)

            manager.loadCart(emptyMap())
            assertEquals(CartItemChange.Reset, result.await())
        } finally {
            manager.close()
        }
    }

    @Test
    fun `first sync initializes cart and releases actions waiting for load`() = runTest {
        val requests = mutableListOf<CartRequest>()
        val manager = AppCartManager(
            parentScope = this,
            repository = repository { request ->
                requests += request
                Result.success(Unit)
            },
            nowMillis = { testScheduler.currentTime },
        )
        try {
            val change = async { manager.setQuantityAwait(1, 2, DispatchMode.IMMEDIATE) }
            runCurrent()
            assertFalse(change.isCompleted)

            assertTrue(manager.syncIfIdle(mapOf(1L to 1)))
            runCurrent()
            assertEquals(CartItemChange.Applied(1, 2), change.await())
            assertEquals(listOf(CartRequest.UpdateProduct(1, 2)), requests)
        } finally {
            manager.close()
        }
    }

    @Test
    fun `sync skips pending changes and applies later when idle`() = runTest {
        val manager = AppCartManager(
            parentScope = this,
            repository = repository { Result.success(Unit) },
            nowMillis = { testScheduler.currentTime },
        )
        try {
            manager.loadCart(emptyMap())
            val change = async { manager.setQuantityAwait(1, 1) }
            runCurrent()

            assertFalse(manager.syncIfIdle(mapOf(1L to 9)))
            assertEquals(1, manager.itemsState.value[1L]?.quantity)

            advanceTimeBy(300)
            runCurrent()
            change.await()
            assertTrue(manager.syncIfIdle(mapOf(1L to 4)))
            assertEquals(4, manager.itemsState.value[1L]?.quantity)
        } finally {
            manager.close()
        }
    }

    @Test
    fun `sync sees clear in flight even with no visible items`() = runTest {
        val response = CompletableDeferred<Result<Unit>>()
        val manager = AppCartManager(
            parentScope = this,
            repository = repository { response.await() },
            nowMillis = { testScheduler.currentTime },
        )
        try {
            manager.loadCart(emptyMap())
            manager.clear()
            runCurrent()

            assertFalse(manager.syncIfIdle(mapOf(1L to 3)))
            response.complete(Result.success(Unit))
            runCurrent()
            assertTrue(manager.syncIfIdle(mapOf(1L to 3)))
            assertEquals(3, manager.itemsState.value[1L]?.quantity)
        } finally {
            manager.close()
        }
    }

    @Test
    fun `invalid quantities do not throw or send a request`() = runTest {
        val requests = mutableListOf<CartRequest>()
        val manager = AppCartManager(
            parentScope = this,
            repository = repository { request ->
                requests += request
                Result.success(Unit)
            },
            nowMillis = { testScheduler.currentTime },
        )
        try {
            manager.loadCart(mapOf(1L to -2))
            assertTrue(manager.itemsState.value.isEmpty())
            assertTrue(manager.setQuantitiesAwait(emptyMap()).items.isEmpty())
            assertTrue(manager.incrementProductsAwait(mapOf(1L to -1, 2L to 0)).items.isEmpty())

            val change = async { manager.setQuantityAwait(1, -3, DispatchMode.IMMEDIATE) }
            runCurrent()
            assertEquals(CartItemChange.NoChange(0), change.await())
            assertTrue(requests.isEmpty())
        } finally {
            manager.close()
        }
    }

    @Test
    fun `closed manager returns reset instead of throwing on submission`() = runTest {
        val manager = AppCartManager(
            parentScope = this,
            repository = repository { Result.success(Unit) },
            nowMillis = { testScheduler.currentTime },
        )
        manager.close()

        manager.setQuantity(1, 1)
        assertEquals(CartItemChange.Reset, manager.setQuantityAwait(1, 1))
        assertFalse(manager.syncIfIdle(emptyMap()))
    }

    private fun repository(execute: suspend (CartRequest) -> Result<Unit>): CartRepository =
        object : CartRepository {
            override suspend fun execute(request: CartRequest): Result<Unit> = execute(request)
        }
}
