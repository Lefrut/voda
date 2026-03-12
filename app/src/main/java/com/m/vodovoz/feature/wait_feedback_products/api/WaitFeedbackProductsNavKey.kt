package com.m.vodovoz.feature.wait_feedback_products.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object WaitFeedbackProductsNavKey : NavKey {
    const val NAV_NAME: String = "feature/wait_feedback_products/WaitFeedbackProducts"
}
