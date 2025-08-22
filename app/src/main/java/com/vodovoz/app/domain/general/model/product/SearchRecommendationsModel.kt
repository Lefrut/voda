package com.vodovoz.app.domain.general.model.product

data class SearchRecommendationsModel(
    val queries: List<String>,
    val section: SectionModel<ProductModel>
)