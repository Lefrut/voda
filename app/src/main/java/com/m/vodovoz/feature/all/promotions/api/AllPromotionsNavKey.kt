package com.m.vodovoz.feature.all.promotions.api

import androidx.navigation3.runtime.NavKey
import com.m.vodovoz.feature.all.promotions.AllPromotionsFragment

data class AllPromotionsNavKey(
    val dataSource: AllPromotionsFragment.DataSource = AllPromotionsFragment.DataSource.All,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/all/promotions/AllPromotions"
    }
}
