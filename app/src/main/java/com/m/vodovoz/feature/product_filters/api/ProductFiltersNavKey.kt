package com.m.vodovoz.feature.product_filters.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import com.m.vodovoz.design_system.model.filters.FiltersUi
import com.m.vodovoz.core.navigation.viewmodel.SharedViewModelStoreNavKey

data class ProductFiltersNavKey(
    val categoryId: Long,
    val filters: FiltersUi,
    override val parentContentKey: String? = null,
) : VodovozNavKey, SharedViewModelStoreNavKey {
    override fun toString(): String = parentContentKey
        ?: "ProductFiltersNavKey(categoryId=$categoryId, filters=$filters)"

    companion object {
        const val NAV_NAME: String = "feature/product_filters/ProductFilters"
    }
}
