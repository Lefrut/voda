package com.m.vodovoz.feature.cart.gifts.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object GiftsNavKey : NavKey {
    const val NAV_NAME: String = "feature/cart/gifts/Gifts"
}
