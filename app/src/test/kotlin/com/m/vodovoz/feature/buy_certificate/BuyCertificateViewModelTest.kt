package com.m.vodovoz.feature.buy_certificate

import CoroutineTestBase
import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.common.resources.ResourcesProvider
import com.m.vodovoz.domain.general.model.product.BuyCertificateCodesModel
import com.m.vodovoz.domain.general.model.product.BuyCertificateDetailsModel
import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BuyCertificateViewModelTest : CoroutineTestBase() {
    private val repository = mockk<VodovozServiceRepository>()
    private val accountManager = mockk<AccountManager>()
    private val resourcesProvider = mockk<ResourcesProvider>()

    @Test
    fun `screen reloads once after login or logout and keeps details for the same account`() = runTest {
        var accountId: Long? = null
        every { accountManager.fetchAccountId() } answers { accountId }
        every { repository.getBuyCertificateDetails() } answers {
            flowOf(Result.success(details(if (accountId == null) "auth" else "oformlenie")))
        }
        val viewModel = BuyCertificateViewModel(repository, resourcesProvider, accountManager)

        viewModel.loadDetailsForCurrentAccount()
        advanceUntilIdle()
        assertEquals("auth", viewModel.stateSnapshot.button.id)
        verify(exactly = 1) { repository.getBuyCertificateDetails() }

        viewModel.loadDetailsForCurrentAccount()
        advanceUntilIdle()
        verify(exactly = 1) { repository.getBuyCertificateDetails() }

        accountId = 42L
        viewModel.loadDetailsForCurrentAccount()
        viewModel.loadDetailsForCurrentAccount()
        advanceUntilIdle()
        assertEquals("oformlenie", viewModel.stateSnapshot.button.id)
        verify(exactly = 2) { repository.getBuyCertificateDetails() }

        accountId = null
        viewModel.loadDetailsForCurrentAccount()
        advanceUntilIdle()
        assertEquals("auth", viewModel.stateSnapshot.button.id)
        verify(exactly = 3) { repository.getBuyCertificateDetails() }
    }

    @Test
    fun `guest response in flight cannot replace details after signing in`() = runTest {
        var accountId: Long? = null
        every { accountManager.fetchAccountId() } answers { accountId }
        every { repository.getBuyCertificateDetails() } answers {
            if (accountId == null) {
                flow {
                    delay(1_000)
                    emit(Result.success(details("auth")))
                }
            } else {
                flowOf(Result.success(details("oformlenie")))
            }
        }
        val viewModel = BuyCertificateViewModel(repository, resourcesProvider, accountManager)

        viewModel.loadDetailsForCurrentAccount()
        runCurrent()
        accountId = 42L
        viewModel.loadDetailsForCurrentAccount()
        advanceUntilIdle()

        assertEquals("oformlenie", viewModel.stateSnapshot.button.id)
        verify(exactly = 2) { repository.getBuyCertificateDetails() }
    }

    private fun details(buttonId: String) = BuyCertificateDetailsModel(
        codes = BuyCertificateCodesModel("", "", ""),
        title = "",
        certificatesTitle = "",
        certificates = emptyList(),
        tabs = emptyList(),
        button = ColorfulButtonModel.Empty.copy(id = buttonId),
        paymentTitle = "",
        paymentTypes = emptyList(),
        faq = null,
    )
}
