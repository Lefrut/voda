package com.m.vodovoz.feature.product_comments.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object ProductCommentsNavKey : NavKey {
    const val NAV_NAME: String = "feature/product_comments/ProductComments"
}
