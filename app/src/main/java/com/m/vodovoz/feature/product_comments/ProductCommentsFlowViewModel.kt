package com.m.vodovoz.feature.product_comments

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.map
import com.m.vodovoz.R
import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.common.resources.ResourcesProvider
import com.m.vodovoz.design_system.model.CommentUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.product_comments.model.CommentMediaUi
import com.m.vodovoz.feature.product_comments.model.ProductCommentsInfoUi
import com.m.vodovoz.feature.product_comments.model.SortUi
import com.m.vodovoz.feature.product_comments.model.toDomain
import com.m.vodovoz.feature.product_comments.model.toUi
import com.m.vodovoz.ui.mvi.Event
import com.m.vodovoz.ui.paging.PagingMviViewModel
import com.m.vodovoz.ui.paging.PagingState
import com.m.vodovoz.ui.paging.emptyCombinedLoadStates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class ProductCommentsFlowViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val accountManager: AccountManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    resourcesProvider: ResourcesProvider,
) : PagingMviViewModel<CommentUi, ProductCommentsFlowViewModel.ProductCommentsState, ProductCommentsFlowViewModel.ProductCommentsEvents>(
    ProductCommentsState(
        currentSort = SortUi(name = resourcesProvider.getString(R.string.all), value = "", order = "")
    )
) {

    private val productId = savedState.get<Long>("productId") ?: navigateBack().let { -1 }
    private val productName = savedState.get<String>("productName") ?: ""
    private val productImage = savedState.get<String>("productImage") ?: ""

    init {
        observeCommentsSort()
    }

    private fun listenUserLoginStatus() = viewModelScope.launch {
        accountManager.observeAccountId().collectLatest { id ->
            if (id == null) updateState { s -> s.copy(showWriteComment = false) }
            else updateState { s -> s.copy(showWriteComment = true) }
        }
    }

    private fun observeCommentsSort() = viewModelScope.launch {
        state.map { it.currentSort }.distinctUntilChanged().collectLatest { sort ->
            val result = vodovozServiceRepository.getProductCommentsPagingResult(productId, sort.toDomain())
            coroutineScope {
                launch {
                    result.flow.map { page -> page.map { it.toUi() } }.collectPagingData()
                }

                if (stateSnapshot.productCommentsInfo == ProductCommentsInfoUi.Empty) {
                    val info = result.meta
                        .first { it.firstPageData != null || it.firstPageError != null }
                        .firstPageData?.toUi() ?: return@coroutineScope
                    updateState { s ->
                        s.copy(productCommentsInfo = info.copy(sorting = listOf(sort) + info.sorting))
                    }
                    listenUserLoginStatus()
                }
            }
        }
    }

    fun selectSort(sort: SortUi) = viewModelScope.launch {
        if (sort == stateSnapshot.currentSort) return@launch
        updateState { s -> s.copy(currentSort = sort, items = emptyList()) }
        sendEvent(ProductCommentsEvents.ScrollToTop)
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

    fun navigateToCommentMedia(
        media: CommentMediaUi,
        mediaList: List<CommentMediaUi>,
    ) = viewModelScope.launch {
        sendEvent(ProductCommentsEvents.GoToCommentMedia(media, mediaList))
    }

    sealed class ProductCommentsEvents : Event {
        data object ScrollToTop : ProductCommentsEvents()
        data object GoBack : ProductCommentsEvents()
        data class GoToCommentMedia(
            val media: CommentMediaUi,
            val mediaList: List<CommentMediaUi>,
        ) : ProductCommentsEvents()
        data class GoToWriteComment(
            val productId: Long,
            val productName: String,
            val productImage: String,
        ) : ProductCommentsEvents()
    }

    @Immutable
    data class ProductCommentsState(
        val productCommentsInfo: ProductCommentsInfoUi = ProductCommentsInfoUi.Empty,
        val currentSort: SortUi = SortUi.Empty,
        val showWriteComment: Boolean = false,
        override val items: List<CommentUi> = emptyList(),
        override val loadStates: CombinedLoadStates = emptyCombinedLoadStates,
    ) : PagingState<CommentUi, ProductCommentsState>() {

        override fun copyPagingState(
            items: List<CommentUi>,
            loadStates: CombinedLoadStates,
        ): ProductCommentsState = copy(items = items, loadStates = loadStates)
    }
}
