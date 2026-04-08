package com.m.vodovoz.feature.addresses.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import com.m.vodovoz.core.navigation.viewmodel.SharedViewModelStoreNavKey
import com.m.vodovoz.feature.addresses.model.AddressScreenTypeUi

data class AddressesNavKey(
    val screenType: AddressScreenTypeUi,
    val addressId: Long? = null,
    override val parentContentKey: String? = null,
) : VodovozNavKey, SharedViewModelStoreNavKey {
    override fun toString(): String = parentContentKey
        ?: "AddressesNavKey(screenType=$screenType, addressId=$addressId)"

    companion object {
        const val NAV_NAME: String = "feature/addresses/Addresses"
    }
}
