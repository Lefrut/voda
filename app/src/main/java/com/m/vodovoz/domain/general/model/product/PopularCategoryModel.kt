package com.m.vodovoz.domain.general.model.product

import com.m.vodovoz.common.model.DataAllAction

data class PopularCategoryModel(
    val id: Long,
    val name: String,
    val picture: String,
    val action: DataAllAction?
)
