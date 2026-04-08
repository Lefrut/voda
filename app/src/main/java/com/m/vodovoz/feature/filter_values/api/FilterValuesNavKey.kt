package com.m.vodovoz.feature.filter_values.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import com.m.vodovoz.design_system.model.filters.FilterUi
import com.m.vodovoz.core.navigation.viewmodel.SharedViewModelStoreNavKey

data class FilterValuesNavKey(
    val categoryId: Long,
    val filter: FilterUi,
    override val parentContentKey: String? = null,
) : VodovozNavKey, SharedViewModelStoreNavKey {
    override fun toString(): String = parentContentKey
        ?: "FilterValuesNavKey(categoryId=$categoryId, filter=$filter)"

    companion object {
        const val NAV_NAME: String = "feature/filter_values/FilterValues"
    }
}
