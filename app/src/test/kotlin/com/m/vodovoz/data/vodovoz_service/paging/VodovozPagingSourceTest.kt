package com.m.vodovoz.data.vodovoz_service.paging

import android.util.Log
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import com.m.vodovoz.data.vodovoz_service.VodovozRequestExecutor
import com.m.vodovoz.data.vodovoz_service.model.VodovozResponseDTO
import com.squareup.moshi.Moshi
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class VodovozPagingSourceTest {
    @Before
    fun mockAndroidLog() {
        mockkStatic(Log::class)
        every { Log.isLoggable(any(), any()) } returns false
        every { Log.d(any(), any()) } returns 0
    }

    @After
    fun restoreAndroidLog() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `first page request supplies both items and metadata`() = runTest {
        val requests = mutableListOf<Int>()
        val result = VodovozPagerFactory.getResult(
            executor = VodovozRequestExecutor(Moshi.Builder().build()),
            request = { page, _ ->
                requests.add(page)
                rawResponse(if (page == 1) "1,2,3" else "4,5,6")
            },
            mapper = { items -> PagingSourceData(items.split(",").map(String::toInt)) },
            metaMapper = { items -> items },
        )
        val dispatcher = StandardTestDispatcher(testScheduler)
        val presenter = object : PagingDataPresenter<Int>(dispatcher) {
            override suspend fun presentPagingDataEvent(event: PagingDataEvent<Int>) = Unit
        }
        backgroundScope.launch(dispatcher) {
            result.flow.collect { presenter.collectFrom(it) }
        }

        testScheduler.runCurrent()
        assertEquals("1,2,3", result.meta.value.firstPageData)
        assertEquals(listOf(1, 2, 3), presenter.snapshot().items)
        assertEquals(listOf(1), requests)

        presenter[2]
        testScheduler.runCurrent()
        assertEquals("1,2,3", result.meta.value.firstPageData)
        assertEquals(listOf(1, 2, 3, 4, 5, 6), presenter.snapshot().items)
        assertEquals(listOf(1, 2), requests)
    }

    @Suppress("UNCHECKED_CAST")
    private fun rawResponse(items: String): Response<VodovozResponseDTO<String>> =
        Response.success(
            """{"status":"Success","data":"$items","navigation":{"TOTAL_PAGES":2}}"""
        ) as Response<VodovozResponseDTO<String>>
}
