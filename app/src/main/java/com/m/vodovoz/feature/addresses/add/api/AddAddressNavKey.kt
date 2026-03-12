package com.m.vodovoz.feature.addresses.add.api

import androidx.navigation3.runtime.NavKey
import com.m.vodovoz.feature.map.model.MapAddressUi

data class AddAddressNavKey(
    val mapAddress: MapAddressUi? = null,
    val addressId: Long? = null,
    val addressName: String? = null,
    val addressType: Int? = null,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/addresses/add/AddAddress"
    }
}
