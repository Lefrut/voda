package com.m.vodovoz.feature.faq.api

import androidx.navigation3.runtime.NavKey
import com.m.vodovoz.feature.buy_certificate.model.FAQUi

data class FAQNavKey(
    val faq: FAQUi,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/faq/FAQ"
    }
}
