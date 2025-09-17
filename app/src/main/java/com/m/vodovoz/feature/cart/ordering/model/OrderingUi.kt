package com.m.vodovoz.feature.cart.ordering.model

import androidx.compose.runtime.Immutable

@Immutable
data class OrderingUi(
    val addressId: Long?,
    val date: String?,
    val timeInterval: String?,
    val recipientPhone: String?,
    val recipientName: String?,
    val recipientEmail: String?,
    val paymentId: String?,
    val paymentChange: String?,
    val paymentBalance: Boolean = false,
    val callYouId: String?,
    val earlierDelivery: Pair<String, String>?
) {
    companion object {
        val Empty = OrderingUi(
            addressId = null,
            date = null,
            timeInterval = null,
            recipientPhone = null,
            recipientName = null,
            recipientEmail = null,
            paymentId = null,
            paymentChange = null,
            callYouId = null,
            paymentBalance = false,
            earlierDelivery = null
        )
    }
}
