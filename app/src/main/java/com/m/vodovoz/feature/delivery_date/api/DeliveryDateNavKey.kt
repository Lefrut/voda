package com.m.vodovoz.feature.delivery_date.api

import androidx.navigation3.runtime.NavKey

data class DeliveryDateNavKey(
    val addressId: Long,
    val earlierDelivery: Boolean = false,
    val date: String? = null,
    val timeInterval: String? = null,
    val queryParams: Map<String, String> = emptyMap(),
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/delivery_date/DeliveryDate"
    }
}
