package com.m.vodovoz.feature.all.orders.history.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object OrdersHistoryNavKey : NavKey {
    const val NAV_NAME: String = "feature/all/orders/history/OrdersHistory"
}
