package com.vodovoz.app.feature.bottom.services.detail.model

sealed class ServiceDetailEvent {
    data class GoToProductDetails(val productId: Long) : ServiceDetailEvent()
    data class GoToAnalogs(val productId: Long) : ServiceDetailEvent()
    data object GoBack : ServiceDetailEvent()

}