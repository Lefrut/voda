package com.m.vodovoz.feature.promotion_details.api

import androidx.navigation3.runtime.NavKey

data class PromotionDetailsNavKey(
    val promotionId: Long,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/promotion_details/PromotionDetails"
    }
}
