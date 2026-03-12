package com.m.vodovoz.feature.product_filters.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object ProductFiltersNavKey : NavKey {
    const val NAV_NAME: String = "feature/product_filters/ProductFilters"
}
