package com.m.vodovoz.feature.addresses.add.api

import androidx.compose.runtime.Immutable
import com.m.vodovoz.core.navigation.VodovozNavKey
import com.m.vodovoz.feature.map.model.MapAddressUi

@Immutable
data class AddAddressNavKey(
    val mapAddress: MapAddressUi? = null,
    val addressId: Long? = null,
    val addressName: String? = null,
    val addressType: Int? = null,
) : VodovozNavKey {
    companion object {
        const val NAV_NAME: String = "feature/addresses/add/AddAddress"
    }
}
