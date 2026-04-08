package com.m.vodovoz.feature.all.brands.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import kotlinx.serialization.Serializable

@Serializable
data object AllBrandsNavKey : VodovozNavKey {
    const val NAV_NAME: String = "feature/all/brands/AllBrands"
}
