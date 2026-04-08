package com.m.vodovoz.feature.all.orders.history.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import kotlinx.serialization.Serializable

@Serializable
data object OrdersHistoryNavKey : VodovozNavKey {
    const val NAV_NAME: String = "feature/all/orders/history/OrdersHistory"
}
