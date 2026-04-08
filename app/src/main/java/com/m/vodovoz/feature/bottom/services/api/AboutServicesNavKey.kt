package com.m.vodovoz.feature.bottom.services.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import kotlinx.serialization.Serializable

@Serializable
data object AboutServicesNavKey : VodovozNavKey {
    const val NAV_NAME: String = "feature/bottom/services/AboutServices"
}
