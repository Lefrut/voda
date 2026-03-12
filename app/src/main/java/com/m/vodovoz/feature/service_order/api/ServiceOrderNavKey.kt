package com.m.vodovoz.feature.service_order.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object ServiceOrderNavKey : NavKey {
    const val NAV_NAME: String = "feature/service_order/ServiceOrder"
}
