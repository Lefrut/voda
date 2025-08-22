package com.vodovoz.app.feature.product_comments.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.domain.general.model.product.ProductCommentsInfoModel
import com.vodovoz.app.domain.general.model.product.SortModel


@Immutable
data class ProductCommentsInfoUi(
    val sorting: List<SortUi>,
    val ratingText: String,
    val commentsCount: Int,
    val commentsCountText: String,
    val images: List<String>,
) {
    companion object {
        val Empty = ProductCommentsInfoUi(
            sorting = emptyList(),
            ratingText = "",
            commentsCount = 0,
            commentsCountText = "",
            images = emptyList()
        )
    }
}

fun ProductCommentsInfoModel.toUi(): ProductCommentsInfoUi {
    return ProductCommentsInfoUi(
        sorting = sorting.map { it.toUi() },
        ratingText = ratingText,
        commentsCount = commentsCount,
        commentsCountText = commentsCountText,
        images = images
    )
}

fun ProductCommentsInfoUi.toDomain(): ProductCommentsInfoModel {
    return ProductCommentsInfoModel(
        sorting = sorting.map { sort -> sort.toDomain() },
        ratingText = ratingText,
        commentsCount = commentsCount,
        commentsCountText = commentsCountText,
        images = images
    )
}

@Immutable
data class SortUi(
    val name: String,
    val value: String,
    val order: String,
) {
    companion object {
        val Empty = SortUi("", "", "")
    }
}

fun SortModel.toUi(): SortUi {
    return SortUi(
        name = name,
        value = value,
        order = order
    )
}

fun List<SortModel>.mapToUi(): List<SortUi> {
    return map { it.toUi() }
}

fun SortUi.toDomain(): SortModel {
    return SortModel(
        name = name,
        value = value,
        order = order
    )
}