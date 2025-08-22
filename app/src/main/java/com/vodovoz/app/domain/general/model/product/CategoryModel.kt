package com.vodovoz.app.domain.general.model.product

data class CategoryModel(
    val id: Int,
    val name: String,
    val depthLevel: Int? = null
)