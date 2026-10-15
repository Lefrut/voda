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
class AppCartManagerSyncTests {
    @Test
    fun `snapshot is rejected after an intervening update has already completed`() = runTest {
        val manager = AppCartManager(this, repository { Result.success(Unit) })
        try {
            manager.loadCart(mapOf(1L to 2))
            val snapshotRevision = manager.awaitIdle()

            assertEquals(CartItemChange.Applied(2, 3), manager.setQuantity(1, 3, DispatchMode.IMMEDIATE))

            assertFalse(manager.syncIfIdle(mapOf(1L to 2), snapshotRevision))
            assertFalse(manager.syncIfIdle(emptyMap(), snapshotRevision))
            assertEquals(3, manager.itemsState.value[1L]?.quantity)
            assertTrue(manager.syncIfIdle(mapOf(1L to 3), manager.awaitIdle()))
        } finally {
            manager.close()
        }
    }

    @Test
    fun `request completion invalidates a snapshot started while that request was in flight`() = runTest {
        val response = CompletableDeferred<Result<Unit>>()
        val manager = AppCartManager(this, repository { response.await() })
        try {
            manager.loadCart(mapOf(1L to 2))
            val change = async { manager.setQuantity(1, 3, DispatchMode.IMMEDIATE) }
            runCurrent()
            val snapshotRevision = manager.revision

            response.complete(Result.success(Unit))
            runCurrent()
            change.await()

            assertFalse(manager.syncIfIdle(mapOf(1L to 2), snapshotRevision))
            assertEquals(3, manager.itemsState.value[1L]?.confirmedQuantity)
        } finally {
            manager.close()
        }
    }

    @Test
    fun `accepted snapshot and reload both invalidate older snapshots`() = runTest {
        val manager = AppCartManager(this, repository { Result.success(Unit) })
        try {
            manager.loadCart(mapOf(1L to 2))
            val snapshotRevision = manager.awaitIdle()
            assertTrue(manager.syncIfIdle(mapOf(1L to 4), snapshotRevision))
            assertFalse(manager.syncIfIdle(mapOf(1L to 2), snapshotRevision))

            val beforeReload = manager.awaitIdle()
            manager.loadCart(emptyMap())
            assertFalse(manager.syncIfIdle(mapOf(1L to 4), beforeReload))
            assertTrue(manager.itemsState.value.isEmpty())
        } finally {
            manager.close()
        }
    }

    @Test
    fun `await idle includes queued noops that never produce a network result`() = runTest {
        val requests = mutableListOf<CartRequest>()
        val manager = AppCartManager(
            this,
            repository { requests += it; Result.success(Unit) },
            nowMillis = { testScheduler.currentTime },
        )
        try {
            manager.loadCart(mapOf(1L to 2))
            val change = async { manager.setQuantity(1, 2) }
            runCurrent()
            val idle = async { manager.awaitIdle() }
            runCurrent()
            assertFalse(idle.isCompleted)

            advanceTimeBy(300)
            runCurrent()
            assertEquals(CartItemChange.NoChange(2), change.await())
            assertEquals(manager.revision, idle.await())
            assertTrue(requests.isEmpty())
        } finally {
            manager.close()
        }
    }

    @Test
    fun `await idle waits for every parallel request`() = runTest {
        val first = CompletableDeferred<Result<Unit>>()
        val second = CompletableDeferred<Result<Unit>>()
        val manager = AppCartManager(this, repository {
            if ((it as CartRequest.UpdateProduct).id == 1L) first.await() else second.await()
        })
        try {
            manager.loadCart(mapOf(1L to 2, 2L to 2))
            val changes = async {
                manager.setQuantities(mapOf(1L to 3, 2L to 3), DispatchMode.IMMEDIATE)
            }
            runCurrent()
            val idle = async { manager.awaitIdle() }
            first.complete(Result.success(Unit))
            runCurrent()
            assertFalse(idle.isCompleted)

            second.complete(Result.success(Unit))
            runCurrent()
            changes.await()
            assertEquals(manager.revision, idle.await())
        } finally {
            manager.close()
        }
    }

    @Test
    fun `clear awaits the server and reports failure without losing confirmed quantities`() = runTest {
        val response = CompletableDeferred<Result<Unit>>()
        val manager = AppCartManager(this, repository { response.await() })
        try {
            manager.loadCart(mapOf(1L to 2))
            val clear = async { manager.clear() }
            runCurrent()
            assertFalse(clear.isCompleted)
            assertEquals(0, manager.itemsState.value[1L]?.quantity)

            val error = IllegalStateException("clear failed")
            response.complete(Result.failure(error))
            runCurrent()
            assertEquals(CartItemChange.Failed(error), clear.await().item(1L))
            assertEquals(2, manager.itemsState.value[1L]?.quantity)
        } finally {
            manager.close()
        }
    }

    @Test
    fun `bottle replacement includes unchanged selections and clears removed bottles`() = runTest {
        val requests = mutableListOf<CartRequest>()
        val manager = AppCartManager(this, repository { requests += it; Result.success(Unit) })
        try {
            manager.loadCart(mapOf(1L to 1, 2L to 1))
            val result = manager.setQuantities(
                mapOf(1L to 1, 2L to 0, 3L to 2),
                DispatchMode.IMMEDIATE,
                RequestKind.REPLACE_BOTTLES,
            )
            assertEquals(listOf(CartRequest.ReplaceBottles(mapOf(1L to 1, 3L to 2))), requests)
            assertEquals(CartItemChange.Applied(1, 0), result.item(2L))

            manager.setQuantities(
                mapOf(1L to 0, 3L to 0),
                DispatchMode.IMMEDIATE,
                RequestKind.REPLACE_BOTTLES,
            )
            assertEquals(CartRequest.ReplaceBottles(emptyMap()), requests.last())
            assertTrue(manager.itemsState.value.isEmpty())
        } finally {
            manager.close()
        }
    }

    @Test
    fun `later single bottle change stays in the pending full replacement`() = runTest {
        val requests = mutableListOf<CartRequest>()
        val manager = AppCartManager(
            this,
            repository { requests += it; Result.success(Unit) },
            nowMillis = { testScheduler.currentTime },
        )
        try {
            manager.loadCart(mapOf(1L to 1, 2L to 1))
            val replacement = async {
                manager.setQuantities(mapOf(1L to 1, 2L to 2), requestKind = RequestKind.REPLACE_BOTTLES)
            }
            runCurrent()
            val single = async { manager.setQuantity(1, 3) }
            runCurrent()
            advanceTimeBy(300)
            runCurrent()

            assertEquals(listOf(CartRequest.ReplaceBottles(mapOf(1L to 3, 2L to 2))), requests)
            assertEquals(CartItemChange.Superseded, replacement.await().item(1L))
            assertEquals(CartItemChange.Applied(1, 3), single.await())
        } finally {
            manager.close()
        }
    }

    @Test
    fun `full bottle replacement waits for busy members without sending a partial group`() = runTest {
        val response = CompletableDeferred<Result<Unit>>()
        val requests = mutableListOf<CartRequest>()
        val manager = AppCartManager(this, repository {
            requests += it
            if (it is CartRequest.UpdateProduct) response.await() else Result.success(Unit)
        })
        try {
            manager.loadCart(mapOf(1L to 1, 2L to 1))
            val single = async { manager.setQuantity(1, 2, DispatchMode.IMMEDIATE) }
            runCurrent()
            val replacement = async {
                manager.setQuantities(mapOf(1L to 3, 2L to 2), DispatchMode.IMMEDIATE, RequestKind.REPLACE_BOTTLES)
            }
            runCurrent()
            assertEquals(listOf(CartRequest.UpdateProduct(1, 2)), requests)
            assertFalse(replacement.isCompleted)

            response.complete(Result.success(Unit))
            runCurrent()
            single.await()
            replacement.await()
            assertEquals(CartRequest.ReplaceBottles(mapOf(1L to 3, 2L to 2)), requests.last())
        } finally {
            manager.close()
        }
    }

    private fun repository(execute: suspend (CartRequest) -> Result<Unit>): CartRepository =
        object : CartRepository {
            override suspend fun execute(request: CartRequest): Result<Unit> = execute(request)
        }
}
