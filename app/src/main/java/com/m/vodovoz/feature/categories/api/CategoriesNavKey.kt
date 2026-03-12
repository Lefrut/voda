package com.m.vodovoz.feature.categories.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object CategoriesNavKey : NavKey {
    const val NAV_NAME: String = "feature/categories/Categories"
}
