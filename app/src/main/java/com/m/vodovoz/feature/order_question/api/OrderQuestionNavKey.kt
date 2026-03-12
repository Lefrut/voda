package com.m.vodovoz.feature.order_question.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object OrderQuestionNavKey : NavKey {
    const val NAV_NAME: String = "feature/order_question/OrderQuestion"
}
