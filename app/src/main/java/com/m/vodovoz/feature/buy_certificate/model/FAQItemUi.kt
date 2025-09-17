package com.m.vodovoz.feature.buy_certificate.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.m.vodovoz.domain.general.model.product.FAQItemModel
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class FAQItemUi(
    val name: String,
    val description: String,
    val expanded: Boolean = false
): Parcelable


fun List<FAQItemModel>.mapToUi(): List<FAQItemUi>{
    return map{ it.toUi() }
}

fun FAQItemModel.toUi(): FAQItemUi{
    return FAQItemUi(name, description)
}
