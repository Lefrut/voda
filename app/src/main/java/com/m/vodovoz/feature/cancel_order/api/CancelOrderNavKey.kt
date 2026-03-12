package com.m.vodovoz.feature.cancel_order.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object CancelOrderNavKey : NavKey {
    const val NAV_NAME: String = "feature/cancel_order/CancelOrder"
}
