package com.m.vodovoz.feature.delivery_date.api

import androidx.navigation3.runtime.NavKey
import com.m.vodovoz.core.navigation.viewmodel.SharedViewModelStoreNavKey

data class DeliveryDateNavKey(
    val addressId: Long,
    val earlierDelivery: Boolean = false,
    val date: String? = null,
    val timeInterval: String? = null,
    val queryParams: Map<String, String> = emptyMap(),
    override val parentContentKey: String? = null,
) : NavKey, SharedViewModelStoreNavKey {
    override fun toString(): String = parentContentKey
        ?: "DeliveryDateNavKey(addressId=$addressId, earlierDelivery=$earlierDelivery, date=$date, timeInterval=$timeInterval, queryParams=$queryParams)"

    companion object {
        const val NAV_NAME: String = "feature/delivery_date/DeliveryDate"
    }
}
