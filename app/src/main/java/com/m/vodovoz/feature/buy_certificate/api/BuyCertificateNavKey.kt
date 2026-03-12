package com.m.vodovoz.feature.buy_certificate.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object BuyCertificateNavKey : NavKey {
    const val NAV_NAME: String = "feature/buy_certificate/BuyCertificate"
}
