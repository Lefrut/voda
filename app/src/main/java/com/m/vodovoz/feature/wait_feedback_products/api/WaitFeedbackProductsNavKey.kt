package com.m.vodovoz.feature.wait_feedback_products.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import kotlinx.serialization.Serializable

@Serializable
data object WaitFeedbackProductsNavKey : VodovozNavKey {
    const val NAV_NAME: String = "feature/wait_feedback_products/WaitFeedbackProducts"
}
