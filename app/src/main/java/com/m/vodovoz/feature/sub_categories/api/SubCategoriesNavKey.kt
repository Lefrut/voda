package com.m.vodovoz.feature.sub_categories.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import com.m.vodovoz.design_system.model.ParentCategoryUi

data class SubCategoriesNavKey(
    val category: ParentCategoryUi,
) : VodovozNavKey {
    companion object {
        const val NAV_NAME: String = "feature/sub_categories/SubCategories"
    }
}
