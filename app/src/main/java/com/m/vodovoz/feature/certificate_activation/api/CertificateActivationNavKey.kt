package com.m.vodovoz.feature.certificate_activation.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import kotlinx.serialization.Serializable

@Serializable
data object CertificateActivationNavKey : VodovozNavKey {
    const val NAV_NAME: String = "feature/certificate_activation/CertificateActivation"
}
