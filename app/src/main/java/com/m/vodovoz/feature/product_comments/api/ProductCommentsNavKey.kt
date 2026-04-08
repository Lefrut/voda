package com.m.vodovoz.feature.product_comments.api

import com.m.vodovoz.core.navigation.VodovozNavKey

data class ProductCommentsNavKey(
    val productId: Long,
    val productName: String,
    val productImage: String,
) : VodovozNavKey {
    companion object {
        const val NAV_NAME: String = "feature/product_comments/ProductComments"
    }
}
