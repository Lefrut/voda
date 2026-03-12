package com.m.vodovoz.feature.payment_method.api

import androidx.navigation3.runtime.NavKey

data class PaymentMethodNavKey(
    val addressId: Long,
    val date: Long,
    val paymentMethodId: String? = null,
    val paymentChange: String?,
    val balance: Boolean? = null,
    val bonuses: Boolean?,
    val bonusesValue: Int?,
    val queryParams: Map<String, String> = emptyMap(),
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/payment_method/PaymentMethod"
    }
}
