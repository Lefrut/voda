package com.m.vodovoz.feature.promotion_details.api

import com.m.vodovoz.core.navigation.VodovozNavKey

data class PromotionDetailsNavKey(
    val promotionId: Long,
) : VodovozNavKey {
    companion object {
        const val NAV_NAME: String = "feature/promotion_details/PromotionDetails"
    }
}
