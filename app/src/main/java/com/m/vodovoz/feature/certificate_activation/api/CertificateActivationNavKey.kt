package com.m.vodovoz.feature.certificate_activation.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object CertificateActivationNavKey : NavKey {
    const val NAV_NAME: String = "feature/certificate_activation/CertificateActivation"
}
