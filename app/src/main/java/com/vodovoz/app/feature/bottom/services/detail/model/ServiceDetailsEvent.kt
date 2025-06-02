package com.vodovoz.app.feature.bottom.services.detail.model

sealed class ServiceDetailsEvent {
    data class GoToProductDetails(val productId: Long) : ServiceDetailsEvent()
    data class GoToAnalogs(val productId: Long) : ServiceDetailsEvent()
    data class GoToServiceOrder(val serviceType: String) : ServiceDetailsEvent()

    data object GoBack : ServiceDetailsEvent()

}