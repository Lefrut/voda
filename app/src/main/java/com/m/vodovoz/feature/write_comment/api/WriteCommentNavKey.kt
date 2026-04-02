package com.m.vodovoz.feature.write_comment.api

import androidx.navigation3.runtime.NavKey
import com.m.vodovoz.core.navigation.viewmodel.SharedViewModelStoreNavKey

data class WriteCommentNavKey(
    val product_id: Long,
    val product_name: String,
    val product_image: String,
    val rating: Int,
    val source: Source = Source.None,
    override val parentContentKey: String? = null,
) : NavKey, SharedViewModelStoreNavKey {
    override fun toString(): String = parentContentKey
        ?: "WriteCommentNavKey(product_id=$product_id, product_name=$product_name, product_image=$product_image, rating=$rating, source=$source)"

    enum class Source {
        None,
        Home,
        WaitFeedbackProducts,
    }

    companion object {
        const val NAV_NAME: String = "feature/write_comment/WriteComment"
    }
}
