package com.m.vodovoz.feature.product_analogs.model

sealed class ProductAnalogsEvent {
    data class GoToProductDetails(val productId: Long) : ProductAnalogsEvent()
    data class GoToProductAnalogs(val productId: Long) : ProductAnalogsEvent()
    data object GoBack : ProductAnalogsEvent()

}