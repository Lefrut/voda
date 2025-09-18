package com.m.vodovoz.domain.general.model.product

import com.m.vodovoz.common.model.ButtonAction
import com.m.vodovoz.domain.general.model.exceptions.VodovozPlaceholderModel

data class SuperTopModel(
    val topSection: SectionModel<CategoryWithProductsModel>,
    val bottomSection: SectionModel<CategoryWithProductsModel>,
){
    companion object{
        val Empty = SuperTopModel(SectionModel.empty(), SectionModel.empty())
    }
}


data class SectionModel<E>(
    val title: String,
    val items: List<E>,
    val button: ButtonModel? = null,
    val placeholder: VodovozPlaceholderModel? = null
){
    companion object{
        fun<T> empty() = SectionModel<T>("", emptyList(), null)
    }
}

data class ButtonModel(
    val name: String,
    val action: ButtonAction,
)


data class CategoryWithProductsModel(
    val id: Long,
    val name: String,
    val products: List<ProductModel>,
)
