package com.m.vodovoz.feature.home.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.domain.general.model.product.UnratedProductModel
import com.m.vodovoz.domain.general.model.product.UnratedProductsSectionModel


@Immutable
data class UnratedProductsSectionUi(
    val title: String,
    val productTitle: String,
    val countProductsText: String,
    val products: List<UnratedProductUi>,
    val buttonText: String,
) {
    companion object {
        val Empty: UnratedProductsSectionUi = UnratedProductsSectionUi("", "", "", emptyList(), "")
    }
}

@Immutable
data class UnratedProductUi(
    val name: String,
    val id: Long,
    val detailPicture: String,
    val rating: Float = 0f,
)

fun UnratedProductsSectionModel.toUi(): UnratedProductsSectionUi {
    return UnratedProductsSectionUi(
        title = title,
        productTitle = productTitle,
        countProductsText = countProductsText,
        products = products.map { it.toUi() },
        buttonText = buttonText
    )
}

fun UnratedProductModel.toUi(): UnratedProductUi {
    return UnratedProductUi(
        name = name,
        id = id,
        detailPicture = detailPicture
    )
}
