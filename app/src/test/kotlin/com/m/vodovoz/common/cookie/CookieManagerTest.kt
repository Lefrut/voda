package com.m.vodovoz.common.cookie

import com.m.vodovoz.common.datastore.DataStorePrefs
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CookieManagerTest {

    private val dataStorePrefs = mockk<DataStorePrefs>(relaxed = true)
    private val cookieManager = CookieManager(dataStorePrefs)

    @Test
    fun `cookie attributes are not persisted or sent`() {
        cookieManager.updateCookieSessionId(
            "PHPSESSID=session-id; Path=/; Expires=Tue, 18 Aug 2026 12:00:00 GMT; HttpOnly"
        )

        verify { dataStorePrefs.putString("cookies", "PHPSESSID=session-id") }
    }

    @Test
    fun `legacy stored cookie is normalized when read`() {
        every { dataStorePrefs.getString("cookies") } returns
            "PHPSESSID=legacy-id; Path=/; HttpOnly"

        assertEquals("PHPSESSID=legacy-id", cookieManager.fetchCookieSessionId())
    }

    @Test
    fun `logout removes cookie and its refresh timestamp`() {
        cookieManager.removeCookieSessionId()

        verify { dataStorePrefs.remove("cookies") }
        verify { dataStorePrefs.remove("last_entire") }
    }

    @Test
    fun `session refresh is requested after fifty minutes`() {
        every { dataStorePrefs.getLong("last_entire") } returns
            System.currentTimeMillis() - 51 * 60 * 1000L

        assertTrue(cookieManager.isOldCookie())
    }

    @Test
    fun `session refresh is not requested before fifty minutes`() {
        every { dataStorePrefs.getLong("last_entire") } returns
            System.currentTimeMillis() - 49 * 60 * 1000L

        assertFalse(cookieManager.isOldCookie())
    }
}
