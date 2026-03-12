package com.m.vodovoz.feature.product_details.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object ProductDetailsNavKey : NavKey {
    const val NAV_NAME: String = "feature/product_details/ProductDetails"
}
