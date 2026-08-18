package com.m.vodovoz.core.network.interceptor

import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.common.cookie.CookieManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CookieHandlerInterceptorTest {

    private val cookieManager = mockk<CookieManager>(relaxed = true)
    private val accountManager = mockk<AccountManager>()
    private val interceptor = CookieHandlerInterceptor(cookieManager, accountManager)

    @Test
    fun `guest response updates expired PHP session cookie`() {
        every { accountManager.isAlreadyLogin() } returns false
        every { cookieManager.fetchCookieSessionId() } returns "PHPSESSID=guest-old"
        every { cookieManager.isOldCookie() } returns true

        val request = Request.Builder().url("https://vodovoz.test/profile").build()
        val response = response(request, "PHPSESSID=guest-new; Path=/; HttpOnly")
        val sentRequest = intercept(request, response)

        assertEquals("PHPSESSID=guest-old", sentRequest.header("Cookie"))
        verify {
            cookieManager.updateCookieSessionId("PHPSESSID=guest-new; Path=/; HttpOnly")
        }
    }

    @Test
    fun `guest response stores PHP session cookie when it is missing`() {
        every { accountManager.isAlreadyLogin() } returns false
        every { cookieManager.fetchCookieSessionId() } returns null

        val request = Request.Builder().url("https://vodovoz.test/home").build()
        val response = response(request, "PHPSESSID=guest-first; Path=/")
        val sentRequest = intercept(request, response)

        assertNull(sentRequest.header("Cookie"))
        verify { cookieManager.updateCookieSessionId("PHPSESSID=guest-first; Path=/") }
    }

    @Test
    fun `guest response cannot replace fresh PHP session cookie`() {
        every { accountManager.isAlreadyLogin() } returns false
        every { cookieManager.fetchCookieSessionId() } returns "PHPSESSID=guest-fresh"
        every { cookieManager.isOldCookie() } returns false

        val request = Request.Builder().url("https://vodovoz.test/profile").build()
        val response = response(request, "PHPSESSID=unexpected; Path=/")
        val sentRequest = intercept(request, response)

        assertEquals("PHPSESSID=guest-fresh", sentRequest.header("Cookie"))
        verify(exactly = 0) { cookieManager.updateCookieSessionId(any()) }
    }

    @Test
    fun `authenticated response cannot replace session cookie`() {
        every { accountManager.isAlreadyLogin() } returns true
        every { cookieManager.fetchCookieSessionId() } returns "PHPSESSID=user"

        val request = Request.Builder().url("https://vodovoz.test/profile").build()
        val response = response(request, "PHPSESSID=unexpected; Path=/")
        val sentRequest = intercept(request, response)

        assertEquals("PHPSESSID=user", sentRequest.header("Cookie"))
        verify(exactly = 0) { cookieManager.updateCookieSessionId(any()) }
    }

    @Test
    fun `empty explicit cookie prevents stored session from being sent`() {
        every { accountManager.isAlreadyLogin() } returns true
        every { cookieManager.fetchCookieSessionId() } returns "PHPSESSID=user"

        val request = Request.Builder()
            .url("https://vodovoz.test/relogin")
            .header("Cookie", "")
            .build()
        val response = response(request)
        val sentRequest = intercept(request, response)

        assertNull(sentRequest.header("Cookie"))
    }

    private fun intercept(request: Request, response: Response): Request {
        val requestSlot = slot<Request>()
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns request
        every { chain.proceed(capture(requestSlot)) } returns response

        interceptor.intercept(chain)

        return requestSlot.captured
    }

    private fun response(request: Request, setCookie: String? = null): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .apply {
                setCookie?.let { header("Set-Cookie", it) }
            }
            .build()
    }
}
