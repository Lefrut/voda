package com.m.vodovoz.feature.addresses.api

import androidx.navigation3.runtime.NavKey
import com.m.vodovoz.feature.addresses.model.AddressScreenTypeUi

data class AddressesNavKey(
    val screenType: AddressScreenTypeUi,
    val addressId: Long? = null,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/addresses/Addresses"
    }
}
