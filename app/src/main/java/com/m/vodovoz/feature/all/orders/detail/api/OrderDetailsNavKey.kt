package com.m.vodovoz.feature.all.orders.detail.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object OrderDetailsNavKey : NavKey {
    const val NAV_NAME: String = "feature/all/orders/detail/OrderDetails"
}
