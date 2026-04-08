package com.m.vodovoz.feature.payment_method.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import com.m.vodovoz.core.navigation.viewmodel.SharedViewModelStoreNavKey

data class PaymentMethodNavKey(
    val addressId: Long,
    val date: Long,
    val paymentMethodId: String? = null,
    val paymentChange: String?,
    val balance: Boolean? = null,
    val bonuses: Boolean?,
    val bonusesValue: Int?,
    val queryParams: Map<String, String> = emptyMap(),
    override val parentContentKey: String? = null,
) : VodovozNavKey, SharedViewModelStoreNavKey {
    override fun toString(): String = parentContentKey
        ?: "PaymentMethodNavKey(addressId=$addressId, date=$date, paymentMethodId=$paymentMethodId, paymentChange=$paymentChange, balance=$balance, bonuses=$bonuses, bonusesValue=$bonusesValue, queryParams=$queryParams)"

    companion object {
        const val NAV_NAME: String = "feature/payment_method/PaymentMethod"
    }
}
