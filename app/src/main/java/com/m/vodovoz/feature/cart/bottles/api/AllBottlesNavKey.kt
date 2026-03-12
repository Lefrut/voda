package com.m.vodovoz.feature.cart.bottles.api

import androidx.navigation3.runtime.NavKey
import com.m.vodovoz.feature.cart.bottles.model.BottleUi

data class AllBottlesNavKey(
    val bottles: List<BottleUi>,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/cart/bottles/AllBottles"
    }
}
