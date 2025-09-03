package com.vodovoz.app.feature.product_comments

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.ui.mvi.Event
import com.vodovoz.app.ui.mvi.MviViewModel
import com.vodovoz.app.ui.mvi.State
import kotlinx.coroutines.flow.update
import com.vodovoz.app.design_system.model.CommentUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.product_comments.model.ProductCommentsInfoUi
import com.vodovoz.app.feature.product_comments.model.SortUi
import com.vodovoz.app.feature.product_comments.model.toDomain
import com.vodovoz.app.feature.product_comments.model.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductCommentsFlowViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val accountManager: AccountManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
) : MviViewModel<ProductCommentsFlowViewModel.ProductCommentsState, ProductCommentsFlowViewModel.ProductCommentsEvents>(
    ProductCommentsState()
) {

    private val productId = savedState.get<Long>("productId") ?: navigateBack().let { -1 }
    private val productName = savedState.get<String>("productName") ?: ""
    private val productImage = savedState.get<String>("productImage") ?: ""

    init {
        fetchProductComments()
    }

    private fun listenUserLoginStatus() = viewModelScope.launch {
        accountManager.observeAccountId().collectLatest { id ->
            if (id == null) updateState { s -> s.copy(showWriteComment = false) }
            else updateState { s -> s.copy(showWriteComment = true) }
        }
    }

    private fun fetchProductComments() = viewModelScope.launch {
        val productCommentsInfoResult =
            vodovozServiceRepository.getProductCommentsInfo(productId).firstOrNull()
        val productCommentsInfo = productCommentsInfoResult?.getOrNull()

        if (productCommentsInfo != null) {
            updateState { s ->
                val uiInfo = productCommentsInfo.toUi()
                val currentSort = uiInfo.sorting.firstOrNull() ?: SortUi.Empty
                s.copy(
                    productCommentsInfo = uiInfo,
                    currentSort = uiInfo.sorting.firstOrNull() ?: SortUi.Empty,
                    pagedComments = vodovozServiceRepository.getProductCommentsPaged(
                        productId, currentSort.toDomain()
                    ).map { pagingData ->
                        pagingData.map { comment ->
                            comment.toUi()
                        }
                    }
                )
            }

            listenUserLoginStatus()
        }
    }


    fun selectSort(sort: SortUi) = viewModelScope.launch {
        sendEvent(ProductCommentsEvents.ScrollToTop)
        updateState { d ->
            d.copy(
                currentSort = sort,
                pagedComments = vodovozServiceRepository.getProductCommentsPaged(
                    productId, sort.toDomain()
                ).map { pagingData ->
                    pagingData.map { comment ->
                        comment.toUi()
                    }
                }
            )
        }
    }

    fun navigateToWriteComment() = viewModelScope.launch {
        sendEvent(
            ProductCommentsEvents.GoToWriteComment(
                productId = productId,
                productName = productName,
                productImage = productImage
            )
        )
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(ProductCommentsEvents.GoBack)
    }

    fun setFullScreenImage(image: String) = viewModelScope.launch {
        updateState { s ->
            s.copy(fullScreenImage = image)
        }
    }

    fun resetFullScreenImage() = viewModelScope.launch {
        updateState { s ->
            s.copy(fullScreenImage = null)
        }
    }

    sealed class ProductCommentsEvents : Event {
        data object ScrollToTop : ProductCommentsEvents()
        data object GoBack : ProductCommentsEvents()
        data class GoToWriteComment(
            val productId: Long,
            val productName: String,
            val productImage: String,
        ) : ProductCommentsEvents()
    }

    @Immutable
    data class ProductCommentsState(
        val productCommentsInfo: ProductCommentsInfoUi = ProductCommentsInfoUi.Empty,
        val pagedComments: Flow<PagingData<CommentUi>> = emptyFlow(),
        val currentSort: SortUi = SortUi.Empty,
        val showWriteComment: Boolean = false,
        val fullScreenImage: String? = null,
    ) : State
}