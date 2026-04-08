package com.m.vodovoz.feature.product_details.api

import com.m.vodovoz.core.navigation.VodovozNavKey

data class ProductDetailsNavKey(
    val productId: Long,
) : VodovozNavKey {
    companion object {
        const val NAV_NAME: String = "feature/product_details/ProductDetails"
    }
}
