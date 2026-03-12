package com.m.vodovoz.feature.order_question.api

import androidx.navigation3.runtime.NavKey

data class OrderQuestionNavKey(
    val orderId: Long,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/order_question/OrderQuestion"
    }
}
