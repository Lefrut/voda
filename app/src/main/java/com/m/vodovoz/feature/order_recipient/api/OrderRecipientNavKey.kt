package com.m.vodovoz.feature.order_recipient.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object OrderRecipientNavKey : NavKey {
    const val NAV_NAME: String = "feature/order_recipient/OrderRecipient"
}
