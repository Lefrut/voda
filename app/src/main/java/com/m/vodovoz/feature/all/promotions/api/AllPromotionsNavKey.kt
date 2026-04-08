package com.m.vodovoz.feature.all.promotions.api

import androidx.navigation3.runtime.NavKey

data class AllPromotionsNavKey(
    val dataSource: DataSource = DataSource.All,
) : NavKey {
    sealed interface DataSource {
        data class ByBanner(val bannerId: Long, val blockId: Long) : DataSource
        data object All : DataSource
    }

    companion object {
        const val NAV_NAME: String = "feature/all/promotions/AllPromotions"
    }
}
