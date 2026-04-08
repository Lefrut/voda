package com.m.vodovoz.feature.buy_certificate.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import kotlinx.serialization.Serializable

@Serializable
data object BuyCertificateNavKey : VodovozNavKey {
    const val NAV_NAME: String = "feature/buy_certificate/BuyCertificate"
}
