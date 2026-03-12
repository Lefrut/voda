package com.m.vodovoz.feature.product_details.api

import androidx.navigation3.runtime.NavKey

data class ProductDetailsNavKey(
    val productId: Long,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/product_details/ProductDetails"
    }
}
