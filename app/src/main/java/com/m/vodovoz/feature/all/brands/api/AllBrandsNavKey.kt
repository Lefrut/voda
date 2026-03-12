package com.m.vodovoz.feature.all.brands.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object AllBrandsNavKey : NavKey {
    const val NAV_NAME: String = "feature/all/brands/AllBrands"
}
