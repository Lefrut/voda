package com.m.vodovoz.feature.cart

import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.cart.AppCartManager
import com.m.vodovoz.common.cart.DispatchMode
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.domain.general.model.cart.CartDetailsModel
import com.m.vodovoz.domain.general.model.cart.CartItemModel
import com.m.vodovoz.domain.general.model.exceptions.EmptyResultException
import com.m.vodovoz.domain.general.respository.CartRepository
import com.m.vodovoz.domain.general.respository.CartRequest
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CartFlowViewModelSyncTests {
    @Test
    fun `stale details are discarded and the next cart result refreshes UI once`() =
        verifyStaleResponse(Result.success(details(2, "stale")))

    @Test
    fun `stale empty cart never replaces UI after a completed update`() =
        verifyStaleResponse(Result.failure(EmptyResultException()))

    @Test
    fun `external refresh reloads quantities and rejects details fetched before notification`() =
        verifyStaleResponse(Result.success(details(2, "stale")), externalUpdate = true)

    @Test
    fun `external refresh rejects an empty response fetched before notification`() =
        verifyStaleResponse(Result.failure(EmptyResultException()), externalUpdate = true)

    private fun verifyStaleResponse(
        staleResult: Result<CartDetailsModel>,
        externalUpdate: Boolean = false,
    ) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val manager = AppCartManager(this, object : CartRepository {
            override suspend fun execute(request: CartRequest) = Result.success(Unit)
        })
        val repository = mockk<VodovozServiceRepository>()
        val tabManager = mockk<TabManager>(relaxed = true)
        val delayedResponse = CompletableDeferred<Result<CartDetailsModel>>()
        var loads = 0
        coEvery { repository.getCartDetails(any()) } returns flow {
            loads++
            emit(when (loads) {
                1 -> Result.success(details(2, "initial"))
                2 -> delayedResponse.await()
                else -> Result.success(details(3, "fresh"))
            })
        }
        val viewModel = CartFlowViewModel(
            cartManager = manager,
            likeManager = mockk(relaxed = true),
            accountManager = mockk(relaxed = true),
            vodovozServiceRepository = repository,
            tabManager = tabManager,
            userPreferencesRepository = mockk(relaxed = true),
        )
        val observedStates = mutableListOf<CartFlowViewModel.CartState>()
        backgroundScope.launch { viewModel.state.collect { observedStates += it } }
        try {
            manager.loadCart(mapOf(1L to 2))
            viewModel.fetchCartDetails().join()
            val fetch = viewModel.fetchCartDetails()
            runCurrent()
            assertFalse(fetch.isCompleted)

            if (externalUpdate) {
                manager.requestRefresh()
                runCurrent()
            } else {
                manager.setQuantity(1, 3, DispatchMode.IMMEDIATE)
            }
            delayedResponse.complete(staleResult)
            runCurrent()
            fetch.join()

            assertEquals(3, loads)
            assertEquals("fresh", viewModel.stateSnapshot.title)
            assertEquals(3, viewModel.stateSnapshot.items1.single().cartQuantity)
            assertEquals(3, manager.itemsState.value[1L]?.confirmedQuantity)
            assertFalse(observedStates.any { it.title == "stale" })
            assertFalse(observedStates.any { it.uiState is CartFlowViewModel.CartUiState.Empty })
        } finally {
            viewModel.viewModelScope.cancel()
            manager.close()
            Dispatchers.resetMain()
        }
    }

    private fun details(quantity: Int, title: String) = CartDetailsModel(
        title = title,
        countText = quantity.toString(),
        items = listOf(CartItemModel(
            id = 1L,
            productId = 1L,
            productName = "Water",
            isFavorite = false,
            canBuy = true,
            discountPercentsText = "",
            discountPrice = 100f,
            priceText = "100",
            currentPrice = 100f,
            basePrice = 100f,
            quantity = quantity,
            depositText = "",
            articleText = "",
            image = "",
            leftItems = 10,
            label = null,
            hasDiscount = false,
            restrictionsCode = 0,
            showcase = false,
            forAdults = null,
            additionalProductsText = null,
        )),
        orderSummary = emptyList(),
        present = null,
        preOrderProductsPopupWindow = null,
        bottlesButton = null,
        promotionalCodeButton = null,
        presentButton = null,
    )
}
