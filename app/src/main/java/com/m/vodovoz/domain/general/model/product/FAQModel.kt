package com.m.vodovoz.domain.general.model.product

data class FAQModel(
    val name: String,
    val image: String,
    val items: List<FAQItemModel>
)
