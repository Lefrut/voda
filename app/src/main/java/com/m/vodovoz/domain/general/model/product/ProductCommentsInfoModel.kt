package com.m.vodovoz.domain.general.model.product

import android.graphics.Bitmap

data class ProductCommentsInfoModel(
    val sorting: List<SortModel>,
    val ratingText: String,
    val commentsCount: Int,
    val commentsCountText: String,
    val images: List<String>,
    val media: List<CommentMediaModel>
) {
    companion object {
        val Empty = ProductCommentsInfoModel(emptyList(), "", 0, "", emptyList(), emptyList())
    }
}


sealed class CommentMediaModel {

    abstract val url: String

    data class Video(
        override val url: String
    ) : CommentMediaModel()

    data class Image(override val url: String) : CommentMediaModel()

}