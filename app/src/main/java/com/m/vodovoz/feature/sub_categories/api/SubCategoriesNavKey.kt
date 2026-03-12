package com.m.vodovoz.feature.sub_categories.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object SubCategoriesNavKey : NavKey {
    const val NAV_NAME: String = "feature/sub_categories/SubCategories"
}
