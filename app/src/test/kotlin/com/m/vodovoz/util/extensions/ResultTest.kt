package com.m.vodovoz.util.extensions

import CoroutineTestBase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultTest : CoroutineTestBase() {

    @Test
    fun catchResult() = runTest {
        val t = Throwable()
        flow<Result<Unit>> { throw t }.catchResult().collect { result ->
            assertEquals(t.toString(), result.exceptionOrNull().toString())
        }
    }

    @Test
    fun resultFailure() {

        val throwable = Throwable()

        assertEquals(
            Result.failure<Unit>(throwable).toString(),
            resultFailure<Unit>(throwable).toString()
        )
    }

    @Test
    fun singleResult() = runTest {
        assertEquals(
            Result.success("1"),
            flowOf(Result.success("1"), Result.success("2")).singleResult()
        )
        val result = flowOf<Result<String>>().singleResult()
        assertTrue(result.exceptionOrNull() is NoSuchElementException)
        assertEquals("Result flow is empty", result.exceptionOrNull()?.message)

    }

    @Test
    fun deferredResult() = runTest {
        val deferred = async {}
        val deferredResult = flowOf(
            Result.success("labuba")
        ).deferredResult()

        assertEquals(
            "labuba",
            deferredResult.await().getOrNull()
        )
        deferred.await()
        assertEquals(
            deferred.key,
            deferredResult.key
        )

    }
}