package com.m.vodovoz.feature.wait_feedback_products.model

import androidx.compose.runtime.Stable
import com.m.vodovoz.design_system.model.VodovozPlaceholderUi

@Stable
sealed interface WaitFeedbackProductsUiState {

    data object Loading: WaitFeedbackProductsUiState
    data object Success: WaitFeedbackProductsUiState
    data object Error: WaitFeedbackProductsUiState
    data class Empty(val placeholder: VodovozPlaceholderUi): WaitFeedbackProductsUiState

}