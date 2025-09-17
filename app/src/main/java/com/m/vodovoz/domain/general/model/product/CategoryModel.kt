package com.m.vodovoz.domain.general.model.product

data class CategoryModel(
    val id: Int,
    val name: String,
    val depthLevel: Int? = null
)