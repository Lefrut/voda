package com.m.vodovoz.domain.general.model.product

import com.m.vodovoz.domain.general.model.user.ForAdultsModel
import com.m.vodovoz.domain.general.model.promotion.BannerModel

data class ProductsSectionModel(
    val banners: List<BannerModel>,
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
            banners = emptyList(),
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
