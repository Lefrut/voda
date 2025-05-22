package com.vodovoz.app.feature.product_analogs.model

sealed class ProductsCollectionEvent {
    data class GoToProductDetails(val productId: Long) : ProductsCollectionEvent()
    data class GoToProductAnalogs(val productId: Long) : ProductsCollectionEvent()
    data object GoBack : ProductsCollectionEvent()

}