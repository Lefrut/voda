package com.m.vodovoz.feature.product_details.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.domain.general.model.product.ProductBonusesModel

@Immutable
data class ProductBonusesUi(
    val bonuses: String,
    val image: String,
)

fun ProductBonusesModel.toUi(): ProductBonusesUi {
    return ProductBonusesUi(image = image, bonuses = bonuses)
}
