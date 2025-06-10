package com.vodovoz.app.domain.general.model

import com.vodovoz.app.domain.general.model.product.ProductModel
import com.vodovoz.app.domain.general.model.product.SectionModel

data class SearchRecommendationsModel(
    val queries: List<String>,
    val section: SectionModel<ProductModel>
)