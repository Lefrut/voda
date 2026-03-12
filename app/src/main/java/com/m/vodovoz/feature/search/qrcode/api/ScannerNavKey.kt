package com.m.vodovoz.feature.search.qrcode.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object ScannerNavKey : NavKey {
    const val NAV_NAME: String = "feature/search/qrcode/Scanner"
}
