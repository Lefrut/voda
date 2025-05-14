package com.vodovoz.app.feature.wait_feedback_products.model

import androidx.compose.runtime.Immutable
import androidx.paging.CombinedLoadStates
import com.vodovoz.app.ui.paging.emptyCombinedLoadStates

@Immutable
data class WaitFeedbackProductsState(
    val title: String = "",
    val products: List<WaitFeedbackProductUi> = emptyList(),
    val loadStates: CombinedLoadStates = emptyCombinedLoadStates,
    val uiState: WaitFeedbackProductsUiState = WaitFeedbackProductsUiState.Loading,
)
