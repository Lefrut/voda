package com.m.vodovoz.feature.cart.bottles.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import com.m.vodovoz.feature.cart.bottles.model.BottleUi

data class AllBottlesNavKey(
    val bottles: List<BottleUi>,
) : VodovozNavKey {
    companion object {
        const val NAV_NAME: String = "feature/cart/bottles/AllBottles"
    }
}
