package com.m.vodovoz.feature.sub_categories.api

import androidx.navigation3.runtime.NavKey
import com.m.vodovoz.design_system.model.ParentCategoryUi

data class SubCategoriesNavKey(
    val category: ParentCategoryUi,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/sub_categories/SubCategories"
    }
}
