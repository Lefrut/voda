package com.vodovoz.app.feature.wait_feedback_products.model

import com.vodovoz.app.design_system.model.VodovozPlaceholderUi

sealed interface WaitFeedbackProductsUiState {

    data object Loading: WaitFeedbackProductsUiState
    data object Success: WaitFeedbackProductsUiState
    data object Error: WaitFeedbackProductsUiState
    data class Empty(val placeholder: VodovozPlaceholderUi): WaitFeedbackProductsUiState

}