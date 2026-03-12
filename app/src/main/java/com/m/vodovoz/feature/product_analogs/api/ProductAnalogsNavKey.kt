package com.m.vodovoz.feature.product_analogs.api

import androidx.navigation3.runtime.NavKey

data class ProductAnalogsNavKey(
    val productId: Long,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/product_analogs/ProductAnalogs"
    }
}
