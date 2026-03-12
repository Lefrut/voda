package com.m.vodovoz.feature.cart.bottles.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object AllBottlesNavKey : NavKey {
    const val NAV_NAME: String = "feature/cart/bottles/AllBottles"
}
