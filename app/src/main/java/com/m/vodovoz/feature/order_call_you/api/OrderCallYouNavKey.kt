package com.m.vodovoz.feature.order_call_you.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import com.m.vodovoz.core.navigation.viewmodel.SharedViewModelStoreNavKey

data class OrderCallYouNavKey(
    val addressId: Long,
    val callYouId: String? = null,
    val queryParams: Map<String, String> = emptyMap(),
    override val parentContentKey: String? = null,
) : VodovozNavKey, SharedViewModelStoreNavKey {
    override fun toString(): String = parentContentKey
        ?: "OrderCallYouNavKey(addressId=$addressId, callYouId=$callYouId, queryParams=$queryParams)"

    companion object {
        const val NAV_NAME: String = "feature/order_call_you/OrderCallYou"
    }
}
