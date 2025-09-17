package com.m.vodovoz.feature.product_details.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.domain.general.model.promotion.PresentInfoModel

@Immutable
data class PresentInfoUi(
    val html: String,
) {
    companion object {
        val Empty = PresentInfoUi("")
    }
}

fun PresentInfoModel.toUi(): PresentInfoUi {
    return PresentInfoUi(html)
}
