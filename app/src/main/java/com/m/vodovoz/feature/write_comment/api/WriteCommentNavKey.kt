package com.m.vodovoz.feature.write_comment.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object WriteCommentNavKey : NavKey {
    const val NAV_NAME: String = "feature/write_comment/WriteComment"
}
