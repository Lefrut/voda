package com.m.vodovoz.feature.search.qrcode.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import kotlinx.serialization.Serializable

@Serializable
data object ScannerNavKey : VodovozNavKey {
    const val NAV_NAME: String = "feature/search/qrcode/Scanner"
}
