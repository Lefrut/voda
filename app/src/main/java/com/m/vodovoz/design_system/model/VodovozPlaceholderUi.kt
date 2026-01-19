package com.m.vodovoz.design_system.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.domain.general.model.exceptions.VodovozPlaceholderModel

@Immutable
data class VodovozPlaceholderUi(
    val title: String,
    val headerHtml: String,
    val descriptionHtml: String,
    val imageUrl: String,
    val button: ColorfulButtonUi? = null,
    val productsSection: VodovozSectionUi<ProductUi>? = null
) {
    companion object {
        val Empty = VodovozPlaceholderUi("", "", "", "", productsSection = VodovozSectionUi.empty())
    }
}

fun VodovozPlaceholderModel.toUi(): VodovozPlaceholderUi {
    return VodovozPlaceholderUi(
        title = title,
        headerHtml = headerHtml,
        descriptionHtml = descriptionHtml,
        imageUrl = imageUrl,
        button = button?.toUi(),
        productsSection = productsSection?.toVodovozSectionUi()
    )
}