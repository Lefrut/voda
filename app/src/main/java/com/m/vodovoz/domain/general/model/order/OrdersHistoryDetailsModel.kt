package com.m.vodovoz.domain.general.model.order

import com.m.vodovoz.domain.general.model.exceptions.VodovozPlaceholderModel
import com.m.vodovoz.domain.general.model.promotion.BannerModel

data class OrdersHistoryDetailsModel(
    val title: String,
    val banners: List<BannerModel>,
    val tabs: List<OrdersHistoryTabModel>,
    val activeTabId: String,
    val placeholder: VodovozPlaceholderModel?
)

data class OrdersHistoryTabModel(
    val id: String,
    val name: String,
    val years: List<String>,
    val selectedYear: String?,
    val items: List<OrdersHistoryItemModel>,
    val pageCount: Int,
    val placeholder: VodovozPlaceholderModel?,
)
