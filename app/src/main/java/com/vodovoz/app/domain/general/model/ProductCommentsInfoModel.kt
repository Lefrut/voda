package com.vodovoz.app.domain.general.model

data class ProductCommentsInfoModel(
    val sorting: List<SortModel>,
    val ratingText: String,
    val commentsCount: Int,
    val commentsCountText: String,
    val images: List<String>
) {
    companion object {
        val Empty = ProductCommentsInfoModel(emptyList(), "", 0, "", emptyList())
    }
}

data class SortModel(
    val name: String,
    val value: String,
    val order: String,
) {
    companion object {
        val Empty = SortModel("", "", "")
    }
}
