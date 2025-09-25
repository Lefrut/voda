package com.m.vodovoz.feature.wait_feedback_products

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import androidx.paging.map
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.domain.general.model.exceptions.EmptyResultException
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.wait_feedback_products.model.WaitFeedbackProductUi
import com.m.vodovoz.feature.wait_feedback_products.model.WaitFeedbackProductsEvent
import com.m.vodovoz.feature.wait_feedback_products.model.WaitFeedbackProductsState
import com.m.vodovoz.feature.wait_feedback_products.model.WaitFeedbackProductsUiState
import com.m.vodovoz.feature.wait_feedback_products.model.toUi
import com.m.vodovoz.ui.paging.PagingMviViewModel
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class WaitFeedbackProductsViewModel @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
) : PagingMviViewModel<WaitFeedbackProductUi, WaitFeedbackProductsState, WaitFeedbackProductsEvent>(
    WaitFeedbackProductsState()
) {

    init {
        viewModelScope.launch { delay(200) }.invokeOnCompletion {
            fetchWaitFeedbackProductsDetails()
        }
    }

    fun fetchWaitFeedbackProductsDetails() = viewModelScope.launch {
        if (stateSnapshot.uiState !is WaitFeedbackProductsUiState.Success) {
            updateState { s ->
                s.copy(uiState = WaitFeedbackProductsUiState.Loading)
            }
        }

        val waitFeedbackProductsResult =
            vodovozServiceRepository.getWaitFeedbackProductsTitle().singleResult()

        waitFeedbackProductsResult.onSuccess { title ->
            updateState { s ->
                s.copy(
                    title = title,
                    uiState = WaitFeedbackProductsUiState.Success
                )
            }

            viewModelScope.launch {
                vodovozServiceRepository.getWaitFeedbackProductsPaged().map { pagingData ->
                    pagingData.map { productModel -> productModel.toUi() }
                }.collectPagingData()
            }
        }.onFailure { t ->
            val uiState = if (t is EmptyResultException && t.placeholder != null) {
                WaitFeedbackProductsUiState.Empty(t.placeholder.toUi())
            } else {
                WaitFeedbackProductsUiState.Error
            }

            if (stateSnapshot.uiState !is WaitFeedbackProductsUiState.Success) {
                updateState { s ->
                    s.copy(uiState = uiState)
                }
            }
        }

    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(WaitFeedbackProductsEvent.GoBack)
    }

    fun navigateToProductDetails(product: WaitFeedbackProductUi) = viewModelScope.launch {
        sendEvent(WaitFeedbackProductsEvent.GoToProductsDetails(product.id))
    }

    fun navigateToCatalog() = viewModelScope.launch {
        sendEvent(WaitFeedbackProductsEvent.GoToCatalog)
    }

    fun navigateToWriteComment(product: WaitFeedbackProductUi, rating: Int) =
        viewModelScope.launch {
            sendEvent(
                WaitFeedbackProductsEvent.GoToWriteComment(
                    product.id,
                    product.name,
                    product.image,
                    rating
                )
            )
        }

    fun removeProduct(productId: Long) = viewModelScope.launch {
        updateState { s ->
            val products = s.items
            s.copy(
                items = products - products.filter { product ->
                    product.id == productId
                }.toSet()
            )
        }
    }

}