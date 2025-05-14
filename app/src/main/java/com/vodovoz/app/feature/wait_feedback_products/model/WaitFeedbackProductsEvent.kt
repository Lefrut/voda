package com.vodovoz.app.feature.wait_feedback_products.model

sealed interface WaitFeedbackProductsEvent {

    data object GoBack : WaitFeedbackProductsEvent
    data object GoToCatalog : WaitFeedbackProductsEvent
    data class GoToProductsDetails(val productId: Long): WaitFeedbackProductsEvent

}