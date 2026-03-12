package com.m.vodovoz.feature.delivery_date.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object DeliveryDateNavKey : NavKey {
    const val NAV_NAME: String = "feature/delivery_date/DeliveryDate"
}
