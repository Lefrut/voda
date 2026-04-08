package com.m.vodovoz.feature.product_analogs.api

import com.m.vodovoz.core.navigation.VodovozNavKey

data class ProductAnalogsNavKey(
    val productId: Long,
) : VodovozNavKey {
    companion object {
        const val NAV_NAME: String = "feature/product_analogs/ProductAnalogs"
    }
}
