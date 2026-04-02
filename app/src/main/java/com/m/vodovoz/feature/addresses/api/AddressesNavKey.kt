package com.m.vodovoz.feature.addresses.api

import androidx.navigation3.runtime.NavKey
import com.m.vodovoz.core.navigation.viewmodel.SharedViewModelStoreNavKey
import com.m.vodovoz.feature.addresses.model.AddressScreenTypeUi

data class AddressesNavKey(
    val screenType: AddressScreenTypeUi,
    val addressId: Long? = null,
    override val parentContentKey: String? = null,
) : NavKey, SharedViewModelStoreNavKey {
    override fun toString(): String = parentContentKey
        ?: "AddressesNavKey(screenType=$screenType, addressId=$addressId)"

    companion object {
        const val NAV_NAME: String = "feature/addresses/Addresses"
    }
}
