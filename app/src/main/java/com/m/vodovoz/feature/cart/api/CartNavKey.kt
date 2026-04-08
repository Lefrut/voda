package com.m.vodovoz.feature.cart.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import kotlinx.serialization.Serializable

@Serializable
data object CartNavKey : VodovozNavKey {
    const val NAV_NAME: String = "feature/cart/Cart"
}
