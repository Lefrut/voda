package com.m.vodovoz.common.cart

import com.m.vodovoz.domain.general.respository.CartRepository
import com.m.vodovoz.domain.general.respository.CartRequest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppCartManagerMigrationTests {
    @Test
    fun `bulk increment updates existing products and batches only new products`() = runTest {
        val requests = mutableListOf<CartRequest>()
        val manager = AppCartManager(this, repository { requests += it; Result.success(Unit) })
        try {
            manager.loadCart(mapOf(1L to 2))
            val result = manager.incrementProducts(
                mapOf(1L to 1, 2L to 2, 3L to 1),
                DispatchMode.IMMEDIATE,
            )

            assertEquals(
                setOf(
                    CartRequest.UpdateProduct(1, 3),
                    CartRequest.AddProducts(mapOf(2L to 2, 3L to 1)),
                ),
                requests.toSet(),
            )
            assertEquals(CartItemChange.Applied(2, 3), result.item(1))
            assertEquals(CartItemChange.Applied(0, 2), result.item(2))
            assertEquals(CartItemChange.Applied(0, 1), result.item(3))
        } finally {
            manager.close()
        }
    }

    @Test
    fun `bulk increment restores only failed products`() = runTest {
        val error = IllegalStateException("update failed")
        val manager = AppCartManager(this, repository {
            if (it is CartRequest.UpdateProduct) Result.failure(error) else Result.success(Unit)
        })
        try {
            manager.loadCart(mapOf(1L to 2))
            val result = manager.incrementProducts(mapOf(1L to 1, 2L to 2), DispatchMode.IMMEDIATE)

            assertEquals(CartItemChange.Failed(error), result.item(1))
            assertEquals(CartItemChange.Applied(0, 2), result.item(2))
            assertEquals(2, manager.itemsState.value[1L]?.quantity)
            assertEquals(2, manager.itemsState.value[2L]?.confirmedQuantity)
            assertTrue(manager.itemsState.value.values.none { it.isLoading })
        } finally {
            manager.close()
        }
    }

    @Test
    fun `rapid increments and decrements use manager quantities`() = runTest {
        val requests = mutableListOf<CartRequest>()
        val manager = AppCartManager(
            this,
            repository { requests += it; Result.success(Unit) },
            nowMillis = { testScheduler.currentTime },
        )
        try {
            manager.loadCart(mapOf(1L to 2))
            val changes = listOf(1, 1, -1).map { delta ->
                async { manager.adjustQuantity(1, delta) }
            }
            runCurrent()
            assertEquals(3, manager.itemsState.value[1L]?.quantity)
            assertFalse(manager.itemsState.value[1L]!!.isLoading)

            advanceTimeBy(300)
            runCurrent()
            assertEquals(
                listOf(CartItemChange.Superseded, CartItemChange.Superseded, CartItemChange.Applied(2, 3)),
                changes.awaitAll(),
            )
            assertEquals(listOf(CartRequest.UpdateProduct(1, 3)), requests)
        } finally {
            manager.close()
        }
    }

    @Test
    fun `multiple products stay editable during debounce and load only while sending`() = runTest {
        val requests = mutableListOf<CartRequest>()
        val response = CompletableDeferred<Result<Unit>>()
        val loadingIds = mutableListOf<Set<Long>>()
        val manager = AppCartManager(
            this,
            repository { requests += it; response.await() },
            nowMillis = { testScheduler.currentTime },
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            manager.loadingProductIds.collect { loadingIds += it }
        }
        try {
            manager.loadCart(emptyMap())
            val first = async { manager.adjustQuantity(1, 1) }
            runCurrent()
            advanceTimeBy(100)
            val second = async { manager.adjustQuantity(1, 1) }
            val other = async { manager.adjustQuantity(2, 1) }
            runCurrent()

            assertEquals(mapOf(1L to 2, 2L to 1), manager.itemsState.value.mapValues { it.value.quantity })
            assertEquals(CartItemChange.Superseded, first.await())
            assertTrue(loadingIds.all { it.isEmpty() })

            advanceTimeBy(299)
            runCurrent()
            assertTrue(requests.isEmpty())
            assertTrue(loadingIds.last().isEmpty())

            advanceTimeBy(1)
            runCurrent()
            assertEquals(listOf(CartRequest.AddProducts(mapOf(1L to 2, 2L to 1))), requests)
            assertEquals(setOf(1L, 2L), loadingIds.last())

            val error = IllegalStateException("request failed")
            response.complete(Result.failure(error))
            runCurrent()
            assertEquals(CartItemChange.Failed(error), second.await())
            assertEquals(CartItemChange.Failed(error), other.await())
            assertTrue(loadingIds.last().isEmpty())
            assertTrue(manager.itemsState.value.values.all { it.quantity == 0 })
        } finally {
            manager.close()
        }
    }

    @Test
    fun `queued edit keeps loading for an active request but not while waiting for its own dispatch`() = runTest {
        val firstResponse = CompletableDeferred<Result<Unit>>()
        val secondResponse = CompletableDeferred<Result<Unit>>()
        val manager = AppCartManager(
            this,
            repository {
                if ((it as CartRequest.UpdateProduct).quantity == 2) firstResponse.await()
                else secondResponse.await()
            },
            nowMillis = { testScheduler.currentTime },
        )
        try {
            manager.loadCart(mapOf(1L to 1))
            val first = async { manager.setQuantity(1, 2, DispatchMode.IMMEDIATE) }
            runCurrent()
            assertTrue(manager.itemsState.value[1L]!!.isLoading)

            val second = async { manager.setQuantity(1, 3) }
            runCurrent()
            assertTrue(manager.itemsState.value[1L]!!.isLoading)

            advanceTimeBy(100)
            firstResponse.complete(Result.success(Unit))
            runCurrent()
            assertEquals(CartItemChange.Applied(1, 2), first.await())
            assertEquals(SyncStatus.QUEUED, manager.itemsState.value[1L]?.status)
            assertFalse(manager.itemsState.value[1L]!!.isLoading)
            assertFalse(second.isCompleted)

            advanceTimeBy(200)
            runCurrent()
            assertTrue(manager.itemsState.value[1L]!!.isLoading)

            secondResponse.complete(Result.success(Unit))
            runCurrent()
            assertEquals(CartItemChange.Applied(2, 3), second.await())
            assertFalse(manager.itemsState.value[1L]!!.isLoading)
        } finally {
            manager.close()
        }
    }

    @Test
    fun `refresh notification invalidates old snapshots without modifying cart or sending a request`() = runTest {
        val requests = mutableListOf<CartRequest>()
        val callbacks = mutableListOf<CartResult>()
        val manager = AppCartManager(
            this,
            repository { requests += it; Result.success(Unit) },
            onResult = { callbacks += it },
        )
        try {
            manager.loadCart(mapOf(1L to 2))
            val snapshotRevision = manager.awaitIdle()
            val notification = async(start = CoroutineStart.UNDISPATCHED) { manager.results.first() }

            manager.requestRefresh()
            runCurrent()

            assertEquals(CartResult.RefreshRequested, notification.await())
            assertEquals(listOf(CartResult.RefreshRequested), callbacks)
            assertFalse(manager.syncIfIdle(emptyMap(), snapshotRevision))
            assertEquals(2, manager.itemsState.value[1L]?.quantity)
            assertTrue(requests.isEmpty())
        } finally {
            manager.close()
        }
    }

    private fun repository(execute: suspend (CartRequest) -> Result<Unit>): CartRepository =
        object : CartRepository {
            override suspend fun execute(request: CartRequest): Result<Unit> = execute(request)
        }
}
