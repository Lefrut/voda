package com.m.vodovoz.feature.faq.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.feature.buy_certificate.model.FAQItemUi

@Immutable
data class FAQState(
    val name: String = "",
    val items: List<FAQItemUi> = emptyList(),
)
