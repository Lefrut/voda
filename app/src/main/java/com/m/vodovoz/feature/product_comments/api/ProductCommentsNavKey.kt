package com.m.vodovoz.feature.product_comments.api

import androidx.navigation3.runtime.NavKey

data class ProductCommentsNavKey(
    val productId: Long,
    val productName: String,
    val productImage: String,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/product_comments/ProductComments"
    }
}
