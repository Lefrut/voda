package com.m.vodovoz.feature.filter_values.api

import androidx.navigation3.runtime.NavKey
import com.m.vodovoz.design_system.model.filters.FilterUi

data class FilterValuesNavKey(
    val categoryId: Long,
    val filter: FilterUi,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/filter_values/FilterValues"
    }
}
