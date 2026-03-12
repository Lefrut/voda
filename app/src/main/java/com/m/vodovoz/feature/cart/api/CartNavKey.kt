package com.m.vodovoz.feature.cart.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object CartNavKey : NavKey {
    const val NAV_NAME: String = "feature/cart/Cart"
}
