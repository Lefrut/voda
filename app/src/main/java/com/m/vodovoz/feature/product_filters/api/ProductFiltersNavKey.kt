package com.m.vodovoz.feature.product_filters.api

import androidx.navigation3.runtime.NavKey
import com.m.vodovoz.design_system.model.filters.FiltersUi

data class ProductFiltersNavKey(
    val categoryId: Long,
    val filters: FiltersUi,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/product_filters/ProductFilters"
    }
}
