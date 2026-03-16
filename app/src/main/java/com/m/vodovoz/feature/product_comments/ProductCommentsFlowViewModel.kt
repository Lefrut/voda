package com.m.vodovoz.feature.product_comments

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import com.m.vodovoz.R
import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.common.resources.ResourcesProvider
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.design_system.model.CommentUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.product_comments.api.ProductCommentsNavKey
import com.m.vodovoz.feature.product_comments.model.CommentMediaUi
import com.m.vodovoz.feature.product_comments.model.ProductCommentsInfoUi
import com.m.vodovoz.feature.product_comments.model.SortUi
import com.m.vodovoz.feature.product_comments.model.toDomain
import com.m.vodovoz.feature.product_comments.model.toUi
import com.m.vodovoz.ui.insets.InsetsVisibilityState
import com.m.vodovoz.ui.mvi.Event
import com.m.vodovoz.ui.mvi.MviViewModel
import com.m.vodovoz.ui.mvi.State
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
@HiltViewModel(assistedFactory = ProductCommentsFlowViewModel.Factory::class)
@Stable
class ProductCommentsFlowViewModel @AssistedInject constructor(
    savedState: SavedStateHandle,
    val tabManager: TabManager,
    val insetsVisibilityState: InsetsVisibilityState,
    private val accountManager: AccountManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
    @Assisted private val navKey: ProductCommentsNavKey?,
) : MviViewModel<ProductCommentsFlowViewModel.ProductCommentsState, ProductCommentsFlowViewModel.ProductCommentsEvents>(
    ProductCommentsState()
) {

    private val productId = navKey?.productId ?: savedState.get<Long>("productId") ?: navigateBack().let { -1 }
    private val productName = navKey?.productName ?: savedState.get<String>("productName") ?: ""
    private val productImage = navKey?.productImage ?: savedState.get<String>("productImage") ?: ""

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
                val uiInfo = with(productCommentsInfo.toUi()) {
                    copy(
                        sorting = buildList {
                            add(
                                SortUi(
                                    name = resourcesProvider.getString(R.string.all),
                                    value = "",
                                    order = ""
                                )
                            )
                            addAll(sorting)
                        }
                    )
                }
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

    fun setFullScreenMedia(media: CommentMediaUi) = viewModelScope.launch {
        updateState { s ->
            s.copy(commentMedia = media)
        }
    }

    fun resetFullScreenMedia() = viewModelScope.launch {
        updateState { s ->
            s.copy(commentMedia = null)
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
        val commentMedia: CommentMediaUi? = null,
    ) : State

    @AssistedFactory
    interface Factory {
        fun create(navKey: ProductCommentsNavKey?): ProductCommentsFlowViewModel
    }

}
