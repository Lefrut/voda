package com.m.vodovoz.feature.cart.ordering.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object OrderingNavKey : NavKey {
    const val NAV_NAME: String = "feature/cart/ordering/Ordering"
}
