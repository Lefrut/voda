package com.vodovoz.app.feature.wait_feedback_products

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.map
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.model.EmptyResultException
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.wait_feedback_products.model.WaitFeedbackProductUi
import com.vodovoz.app.feature.wait_feedback_products.model.WaitFeedbackProductsEvent
import com.vodovoz.app.feature.wait_feedback_products.model.WaitFeedbackProductsState
import com.vodovoz.app.feature.wait_feedback_products.model.WaitFeedbackProductsUiState
import com.vodovoz.app.feature.wait_feedback_products.model.toUi
import com.vodovoz.app.ui.mvi.MviViewModel
import com.vodovoz.app.ui.paging.PagingDataListener
import com.vodovoz.app.ui.paging.copy
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class WaitFeedbackProductsViewModel @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
) : MviViewModel<WaitFeedbackProductsState, WaitFeedbackProductsEvent>(WaitFeedbackProductsState()) {


    private val pagingProductsListener = PagingDataListener(
        onUpdateItems = { itemSnapshotList ->
            _state.update { s ->
                val pagedProducts = itemSnapshotList.mapNotNull { product ->
                    product
                }
                s.copy(products = pagedProducts)
            }
        }
    )

    private fun listenProductsLoadStates() = viewModelScope.launch {
        pagingProductsListener.collectLoadState { combinedLoadStates ->
            val refreshState = when {
                combinedLoadStates.refresh is LoadState.Loading && stateSnapshot.products.isNotEmpty() -> {
                    stateSnapshot.loadStates.refresh
                }

                else -> combinedLoadStates.refresh
            }

            _state.update { s ->
                s.copy(
                    loadStates = combinedLoadStates.copy(
                        refresh = refreshState
                    )
                )
            }
        }


    }

    init {
        listenProductsLoadStates()
        viewModelScope.launch { delay(200) }.invokeOnCompletion {
            fetchWaitFeedbackProductsDetails()
        }
    }

    fun notifyPagingProducts(index: Int) = viewModelScope.launch {
        kotlin.runCatching { pagingProductsListener[index] }
    }


    fun fetchWaitFeedbackProductsDetails() = viewModelScope.launch {
        if (stateSnapshot.uiState !is WaitFeedbackProductsUiState.Success) {
            _state.update { s ->
                s.copy(uiState = WaitFeedbackProductsUiState.Loading)
            }
        }

        val waitFeedbackProductsResult =
            vodovozServiceRepository.getWaitFeedbackProductsTitle().singleResult()

        waitFeedbackProductsResult.onSuccess { title ->
            _state.update { s ->
                s.copy(
                    title = title,
                    uiState = WaitFeedbackProductsUiState.Success
                )
            }

            viewModelScope.launch {
                vodovozServiceRepository.getWaitFeedbackProductsPaged().map { pagingData ->
                    pagingData.map { productModel -> productModel.toUi() }
                }.collect { pagingData ->
                    pagingProductsListener.collectPagingData(pagingData)
                }
            }
        }.onFailure { t ->
            val uiState = if (t is EmptyResultException && t.placeholder != null) {
                WaitFeedbackProductsUiState.Empty(t.placeholder.toUi())
            } else {
                WaitFeedbackProductsUiState.Error
            }

            if (stateSnapshot.uiState !is WaitFeedbackProductsUiState.Success) {
                _state.update { s ->
                    s.copy(uiState = uiState)
                }
            }
        }

    }

    fun navigateBack() = viewModelScope.launch {
        _events.emit(WaitFeedbackProductsEvent.GoBack)
    }

    fun navigateToProductDetails(product: WaitFeedbackProductUi) = viewModelScope.launch {
        _events.emit(WaitFeedbackProductsEvent.GoToProductsDetails(product.id))
    }

    fun navigateToCatalog() = viewModelScope.launch {
        _events.emit(WaitFeedbackProductsEvent.GoToCatalog)
    }

    fun navigateToWriteComment(product: WaitFeedbackProductUi, rating: Int) =
        viewModelScope.launch {
            _events.emit(
                WaitFeedbackProductsEvent.GoToWriteComment(
                    product.id,
                    product.name,
                    product.image,
                    rating
                )
            )
        }

    fun removeProduct(productId: Long) = viewModelScope.launch {
        _state.update { s ->
            val products = s.products
            s.copy(
                products = products - products.filter { product ->
                    product.id == productId
                }.toSet()
            )
        }
    }

}