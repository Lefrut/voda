package com.m.vodovoz.common.cart

import com.m.vodovoz.domain.general.respository.CartRepository
import com.m.vodovoz.domain.general.respository.CartRequest
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
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
