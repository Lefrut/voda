package com.m.vodovoz.feature.search.qrcode.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import kotlinx.serialization.Serializable

@Serializable
data object QrCodeNavKey : VodovozNavKey {
    const val NAV_NAME: String = "feature/search/qrcode/QrCode"
}
