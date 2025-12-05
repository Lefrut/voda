package com.m.vodovoz.domain.general.model.order

import com.m.vodovoz.domain.general.model.promotion.BannerModel

data class OrdersHistoryDetailsModel(
    val title: String,
    val filters: List<OrderFilterModel>,
    val banners: List<BannerModel>,
    val items: List<OrdersHistoryItemModel>
)
