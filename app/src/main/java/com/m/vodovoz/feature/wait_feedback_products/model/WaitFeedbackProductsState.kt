package com.m.vodovoz.feature.wait_feedback_products.model

import androidx.compose.runtime.Immutable
import androidx.paging.CombinedLoadStates
import com.m.vodovoz.ui.paging.PagingState
import com.m.vodovoz.ui.paging.emptyCombinedLoadStates

@Immutable
data class WaitFeedbackProductsState(
    val title: String = "",
    override val items: List<WaitFeedbackProductUi> = emptyList(),
    override val loadStates: CombinedLoadStates = emptyCombinedLoadStates,
    val uiState: WaitFeedbackProductsUiState = WaitFeedbackProductsUiState.Loading,
) : PagingState<WaitFeedbackProductUi, WaitFeedbackProductsState>() {

    override fun copyPagingState(
        items: List<WaitFeedbackProductUi>,
        loadStates: CombinedLoadStates,
    ): WaitFeedbackProductsState = copy(items = items, loadStates = loadStates)
}
