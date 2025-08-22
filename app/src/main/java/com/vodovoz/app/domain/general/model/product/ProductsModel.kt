package com.vodovoz.app.domain.general.model.product

import com.vodovoz.app.domain.general.model.user.ForAdultsModel

data class ProductsSectionModel(
    val title: String,
    val sortingTitle: String,
    val productsQuantityText: String,
    val sorting: List<SortModel>,
    val products: List<ProductModel>,
    val categories: List<CategoryModel>,
    val share: ShareModel,
    val forAdults: ForAdultsModel?,
) {
    companion object {
        val Empty = ProductsSectionModel(
            title = "",
            sortingTitle = "",
            productsQuantityText = "",
            sorting = emptyList(),
            products = emptyList(),
            categories = emptyList(),
            share = ShareModel.Empty,
            forAdults = null
        )
    }
}

data class ShareModel(
    val url: String,
    val text: String,
) {
    companion object {
        val Empty = ShareModel("", "")
    }
}