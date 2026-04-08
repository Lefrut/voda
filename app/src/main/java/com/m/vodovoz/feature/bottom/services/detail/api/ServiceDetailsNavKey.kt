package com.m.vodovoz.feature.bottom.services.detail.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import kotlinx.serialization.Serializable

@Serializable
data object ServiceDetailsNavKey : VodovozNavKey {
    const val NAV_NAME: String = "feature/bottom/services/detail/ServiceDetails"
}
