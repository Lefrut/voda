package com.m.vodovoz.feature.order_recipient.api

import androidx.navigation3.runtime.NavKey

data class OrderRecipientNavKey(
    val addressId: Long,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/order_recipient/OrderRecipient"
    }
}
