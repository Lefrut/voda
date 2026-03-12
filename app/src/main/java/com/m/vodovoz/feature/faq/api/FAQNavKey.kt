package com.m.vodovoz.feature.faq.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object FAQNavKey : NavKey {
    const val NAV_NAME: String = "feature/faq/FAQ"
}
