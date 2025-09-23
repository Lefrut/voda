package com.m.vodovoz.util.extensions

import CoroutineTestBase
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultTest : CoroutineTestBase() {

    @Test
    fun catchResult() {

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
    fun deferredResult() {
    }
}