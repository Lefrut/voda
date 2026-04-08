package com.m.vodovoz.feature.order_recipient.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import com.m.vodovoz.core.navigation.viewmodel.SharedViewModelStoreNavKey

data class OrderRecipientNavKey(
    val addressId: Long,
    override val parentContentKey: String? = null,
) : VodovozNavKey, SharedViewModelStoreNavKey {
    override fun toString(): String = parentContentKey
        ?: "OrderRecipientNavKey(addressId=$addressId)"

    companion object {
        const val NAV_NAME: String = "feature/order_recipient/OrderRecipient"
    }
}
