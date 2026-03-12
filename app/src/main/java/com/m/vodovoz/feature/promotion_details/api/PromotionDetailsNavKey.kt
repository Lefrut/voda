package com.m.vodovoz.feature.promotion_details.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object PromotionDetailsNavKey : NavKey {
    const val NAV_NAME: String = "feature/promotion_details/PromotionDetails"
}
