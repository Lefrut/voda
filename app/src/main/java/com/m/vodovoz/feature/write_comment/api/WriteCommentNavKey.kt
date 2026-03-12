package com.m.vodovoz.feature.write_comment.api

import androidx.navigation3.runtime.NavKey

data class WriteCommentNavKey(
    val product_id: Long,
    val product_name: String,
    val product_image: String,
    val rating: Int,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/write_comment/WriteComment"
    }
}
