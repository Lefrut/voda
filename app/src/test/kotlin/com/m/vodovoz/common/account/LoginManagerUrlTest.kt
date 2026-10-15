package com.m.vodovoz.common.account

import com.m.vodovoz.common.cart.AppCartManager
import com.m.vodovoz.core.network.VodovozWebConfig
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LoginManagerUrlTest {

    private lateinit var accountManager: AccountManager
    private lateinit var vodovozServiceRepository: VodovozServiceRepository
    private lateinit var loginManager: LoginManager
    private lateinit var cartManager: AppCartManager

    @Before
    fun setUp() {
        accountManager = mockk(relaxed = true)
        cartManager = mockk(relaxed = true)
        vodovozServiceRepository = mockk {
            every { relogin() } returns flowOf(Result.success(true))
        }
        loginManager = LoginManager(
            accountManager = accountManager,
            likeManager = mockk(relaxed = true),
            firebaseTokenManager = mockk(relaxed = true),
            vodovozServiceRepository = vodovozServiceRepository,
            cartManager = cartManager,
        )
        VodovozWebConfig.setProdMode(VodovozWebConfig.VODOVOZ_BASE_URL)
    }

    @After
    fun tearDown() {
        VodovozWebConfig.setProdMode(VodovozWebConfig.VODOVOZ_BASE_URL)
    }

    @Test
    fun `successful personal login replaces empty user url with base url`() = runTest {
        loginManager.initializeUserSession(
            userId = 42,
            userToken = "token",
            userUrl = "",
        )

        assertEquals(VodovozWebConfig.VODOVOZ_BASE_URL, VodovozWebConfig.VODOVOZ_URL)
        verifyOrder {
            accountManager.updateUserUrl(VodovozWebConfig.VODOVOZ_BASE_URL)
            accountManager.updateUserId(42)
            accountManager.updateUserToken("token")
            vodovozServiceRepository.relogin()
            cartManager.requestRefresh()
        }
    }

    @Test
    fun `successful test login does not overwrite persisted production user url`() = runTest {
        val testUrl = "https://test.vodovoz.test/"
        VodovozWebConfig.setTestMode(testUrl)

        loginManager.initializeUserSession(
            userId = 42,
            userToken = "token",
            userUrl = "",
        )

        assertEquals(testUrl, VodovozWebConfig.VODOVOZ_URL)
        verify(exactly = 0) { accountManager.updateUserUrl(any()) }
        verify { accountManager.updateUserId(42) }
        verify { accountManager.updateUserToken("token") }
        verify { vodovozServiceRepository.relogin() }
        verify(exactly = 1) { cartManager.requestRefresh() }
    }
}
