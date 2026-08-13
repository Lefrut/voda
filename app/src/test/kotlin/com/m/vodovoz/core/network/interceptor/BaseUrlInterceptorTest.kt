package com.m.vodovoz.core.network.interceptor

import com.m.vodovoz.core.network.VodovozWebConfig
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class BaseUrlInterceptorTest {

    private val interceptor = BaseUrlInterceptor()

    @After
    fun restoreBaseUrl() {
        VodovozWebConfig.setProdMode(VodovozWebConfig.VODOVOZ_BASE_URL)
    }

    @Test
    fun `interceptor uses current config url and preserves request path and query`() {
        VodovozWebConfig.setProdMode("https://business.vodovoz.test/root/")

        val actualUrl = intercept("https://initial.test/newmobile_new/profile/?page=2")

        assertEquals(
            "https://business.vodovoz.test/root/newmobile_new/profile/?page=2",
            actualUrl.toString()
        )
    }

    @Test
    fun `interceptor reads changed config url for every request`() {
        VodovozWebConfig.setProdMode("https://first.vodovoz.test/")
        val firstUrl = intercept("https://initial.test/newmobile_new/catalog/")

        VodovozWebConfig.setProdMode("https://second.vodovoz.test/")
        val secondUrl = intercept("https://initial.test/newmobile_new/catalog/")

        assertEquals("first.vodovoz.test", firstUrl.host)
        assertEquals("second.vodovoz.test", secondUrl.host)
    }

    private fun intercept(originalUrl: String): HttpUrl {
        val request = Request.Builder().url(originalUrl).build()
        val requestSlot = slot<Request>()
        val chain = mockk<Interceptor.Chain>()

        every { chain.request() } returns request
        every { chain.proceed(capture(requestSlot)) } returns mockk<Response>()

        interceptor.intercept(chain)

        return requestSlot.captured.url
    }
}
