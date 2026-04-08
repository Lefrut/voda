package com.m.vodovoz.feature.faq.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import com.m.vodovoz.feature.buy_certificate.model.FAQUi

data class FAQNavKey(
    val faq: FAQUi,
) : VodovozNavKey {
    companion object {
        const val NAV_NAME: String = "feature/faq/FAQ"
    }
}
