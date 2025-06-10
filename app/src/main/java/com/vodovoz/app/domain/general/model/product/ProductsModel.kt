package com.vodovoz.app.domain.general.model.product

import com.vodovoz.app.domain.general.model.CategoryModel
import com.vodovoz.app.domain.general.model.user.ForAdultsModel
import com.vodovoz.app.domain.general.model.SortModel

data class ProductsSectionModel(
    val title: String,
    val sortingTitle: String,
    val productsQuantityText: String,
    val sorting: List<SortModel>,
    val products: List<ProductModel>,
    val categories: List<CategoryModel>,
    val share: ShareModel,
    val forAdults: ForAdultsModel?
)

data class ShareModel(
    val url: String,
    val text: String
){
    companion object{
        val Empty = ShareModel("","")
    }
}