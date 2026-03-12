package com.m.vodovoz.feature.about_product.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object AboutProductNavKey : NavKey {
    const val NAV_NAME: String = "feature/about_product/AboutProduct"
}
