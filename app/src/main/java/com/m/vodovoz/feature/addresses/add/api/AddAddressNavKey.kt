package com.m.vodovoz.feature.addresses.add.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object AddAddressNavKey : NavKey {
    const val NAV_NAME: String = "feature/addresses/add/AddAddress"
}
