package com.m.vodovoz.feature.payment_method.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object PaymentMethodNavKey : NavKey {
    const val NAV_NAME: String = "feature/payment_method/PaymentMethod"
}
