package com.m.vodovoz.feature.product_details.detail_media.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object DetailMediaNavKey : NavKey {
    const val NAV_NAME: String = "feature/product_details/detail_media/DetailMedia"
}
