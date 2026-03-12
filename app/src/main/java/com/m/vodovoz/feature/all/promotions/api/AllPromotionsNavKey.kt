package com.m.vodovoz.feature.all.promotions.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object AllPromotionsNavKey : NavKey {
    const val NAV_NAME: String = "feature/all/promotions/AllPromotions"
}
