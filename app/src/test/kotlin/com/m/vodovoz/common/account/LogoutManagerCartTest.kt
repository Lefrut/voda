package com.m.vodovoz.common.account

import com.m.vodovoz.common.cart.AppCartManager
import com.m.vodovoz.common.cookie.CookieManager
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LogoutManagerCartTest {
    @Test
    fun `successful logout requests cart refresh after credentials and cookie are cleared`() = runTest {
        val repository = mockk<VodovozServiceRepository> {
            every { logout() } returns flowOf(Result.success(Unit))
        }
        val account = mockk<AccountManager>(relaxed = true)
        val cookie = mockk<CookieManager>(relaxed = true)
        val cart = mockk<AppCartManager>(relaxed = true)
        val manager = LogoutManager(
            accountManager = account,
            vodovozServiceRepository = repository,
            firebaseTokenManager = mockk(relaxed = true),
            cookieManager = cookie,
            waterAppHelper = mockk(relaxed = true),
            tabManager = mockk(relaxed = true),
            cartManager = cart,
        )

        assertEquals(Result.success(Unit), manager.logout().single())
        verifyOrder {
            account.removeUserId()
            account.removeUserToken()
            cookie.removeCookieSessionId()
            cart.requestRefresh()
        }
        verify(exactly = 1) { cart.requestRefresh() }
    }

    @Test
    fun `failed logout does not request cart refresh`() = runTest {
        val error = IllegalStateException("logout failed")
        val repository = mockk<VodovozServiceRepository> {
            every { logout() } returns flowOf(Result.failure(error))
        }
        val cart = mockk<AppCartManager>(relaxed = true)
        val manager = LogoutManager(
            accountManager = mockk(relaxed = true),
            vodovozServiceRepository = repository,
            firebaseTokenManager = mockk(relaxed = true),
            cookieManager = mockk(relaxed = true),
            waterAppHelper = mockk(relaxed = true),
            tabManager = mockk(relaxed = true),
            cartManager = cart,
        )

        assertEquals(error, manager.logout().single().exceptionOrNull())
        verify(exactly = 0) { cart.requestRefresh() }
    }
}
