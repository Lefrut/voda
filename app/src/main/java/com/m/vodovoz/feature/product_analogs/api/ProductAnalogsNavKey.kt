package com.m.vodovoz.feature.product_analogs.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object ProductAnalogsNavKey : NavKey {
    const val NAV_NAME: String = "feature/product_analogs/ProductAnalogs"
}
