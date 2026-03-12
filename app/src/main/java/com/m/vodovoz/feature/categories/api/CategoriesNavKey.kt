package com.m.vodovoz.feature.categories.api

import androidx.navigation3.runtime.NavKey
import com.m.vodovoz.feature.home.model.CategoryUi

data class CategoriesNavKey(
    val categoryList: Array<CategoryUi>,
    val category: CategoryUi,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/categories/Categories"
    }
}
