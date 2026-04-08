package com.m.vodovoz.feature.all.promotions.api

import com.m.vodovoz.core.navigation.VodovozNavKey

data class AllPromotionsNavKey(
    val dataSource: DataSource = DataSource.All,
) : VodovozNavKey {
    sealed interface DataSource {
        data class ByBanner(val bannerId: Long, val blockId: Long) : DataSource
        data object All : DataSource
    }

    companion object {
        const val NAV_NAME: String = "feature/all/promotions/AllPromotions"
    }
}
