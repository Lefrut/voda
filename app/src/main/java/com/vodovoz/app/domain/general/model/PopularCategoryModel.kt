package com.vodovoz.app.domain.general.model

import com.vodovoz.app.common.model.DataAllAction

data class PopularCategoryModel(
    val id: Long,
    val name: String,
    val picture: String,
    val action: DataAllAction?
)
