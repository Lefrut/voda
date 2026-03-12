package com.m.vodovoz.feature.order_call_you.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object OrderCallYouNavKey : NavKey {
    const val NAV_NAME: String = "feature/order_call_you/OrderCallYou"
}
