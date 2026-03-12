package com.m.vodovoz.feature.addresses.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object AddressesNavKey : NavKey {
    const val NAV_NAME: String = "feature/addresses/Addresses"
}
