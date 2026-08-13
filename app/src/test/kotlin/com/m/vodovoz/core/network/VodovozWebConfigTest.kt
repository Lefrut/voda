package com.m.vodovoz.core.network

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class VodovozWebConfigTest {

    @After
    fun restoreBaseUrl() {
        VodovozWebConfig.setProdMode(VodovozWebConfig.VODOVOZ_BASE_URL)
    }

    @Test
    fun `prod mode uses base url when user url is empty`() {
        VodovozWebConfig.setProdMode("")

        assertEquals(VodovozWebConfig.VODOVOZ_BASE_URL, VodovozWebConfig.VODOVOZ_URL)
        assertEquals(false, VodovozWebConfig.isTestMode)
    }

    @Test
    fun `prod mode uses business user url when it is present`() {
        val businessUrl = "https://business.vodovoz.test/"

        VodovozWebConfig.setProdMode(businessUrl)

        assertEquals(businessUrl, VodovozWebConfig.VODOVOZ_URL)
        assertEquals(false, VodovozWebConfig.isTestMode)
    }

    @Test
    fun `test mode rejects empty url without changing mode`() {
        VodovozWebConfig.setProdMode("https://business.vodovoz.test/")

        val changed = VodovozWebConfig.setTestMode(" ")

        assertEquals(false, changed)
        assertEquals(false, VodovozWebConfig.isTestMode)
        assertEquals("https://business.vodovoz.test/", VodovozWebConfig.VODOVOZ_URL)
    }

    @Test
    fun `empty auth url keeps active test url`() {
        val testUrl = "https://test.vodovoz.test/"
        VodovozWebConfig.setTestMode(testUrl)

        val actualUrl = VodovozWebConfig.setAuthUrl("")

        assertEquals(testUrl, actualUrl)
        assertEquals(testUrl, VodovozWebConfig.VODOVOZ_URL)
    }

    @Test
    fun `empty auth url switches previous business url to production base url`() {
        VodovozWebConfig.setProdMode("https://business.vodovoz.test/")

        val actualUrl = VodovozWebConfig.setAuthUrl("")

        assertEquals(VodovozWebConfig.VODOVOZ_BASE_URL, actualUrl)
        assertEquals(VodovozWebConfig.VODOVOZ_BASE_URL, VodovozWebConfig.VODOVOZ_URL)
    }

    @Test
    fun `completed test auth does not return url for persistence`() {
        VodovozWebConfig.setTestMode("https://test.vodovoz.test/")

        val urlToPersist = VodovozWebConfig.completeAuth("https://test-business.test/")

        assertEquals(null, urlToPersist)
        assertEquals("https://test-business.test/", VodovozWebConfig.VODOVOZ_URL)
    }

    @Test
    fun `reset after test business auth restores test mode url`() {
        val testUrl = "https://test.vodovoz.test/"
        VodovozWebConfig.setTestMode(testUrl)
        VodovozWebConfig.setAuthUrl("https://test-business.test/")

        val urlToPersist = VodovozWebConfig.resetAuthUrl()

        assertEquals(null, urlToPersist)
        assertEquals(testUrl, VodovozWebConfig.VODOVOZ_URL)
        assertEquals(true, VodovozWebConfig.isTestMode)
    }

    @Test
    fun `completed personal production auth returns non empty base url`() {
        VodovozWebConfig.setProdMode("https://business.vodovoz.test/")

        val urlToPersist = VodovozWebConfig.completeAuth("")

        assertEquals(VodovozWebConfig.VODOVOZ_BASE_URL, urlToPersist)
        assertEquals(VodovozWebConfig.VODOVOZ_BASE_URL, VodovozWebConfig.VODOVOZ_URL)
    }

    @Test
    fun `derived urls use current url`() {
        VodovozWebConfig.setProdMode("https://current.vodovoz.test/root/")

        assertEquals(
            "https://current.vodovoz.test/root/newmobile_new/",
            VodovozWebConfig.VODOVOZ_API_URL
        )
        assertEquals(
            "https://current.vodovoz.test/root/newmobile_new/informatsiya/omagazine.php",
            VodovozWebConfig.ABOUT_SHOP_URL
        )
    }

    @Test
    fun `buildUrl joins relative path with current url`() {
        VodovozWebConfig.setProdMode("https://current.vodovoz.test/root/")

        val actualUrl = VodovozWebConfig.buildUrl("/images/product.png")

        assertEquals("https://current.vodovoz.test/root/images/product.png", actualUrl)
    }

    @Test
    fun `buildUrl keeps absolute url unchanged`() {
        VodovozWebConfig.setProdMode("https://current.vodovoz.test/")
        val absoluteUrl = "https://cdn.vodovoz.test/images/product.png"

        val actualUrl = VodovozWebConfig.buildUrl(absoluteUrl)

        assertEquals(absoluteUrl, actualUrl)
    }
}
