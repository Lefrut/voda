package com.m.vodovoz.feature.order_question.api

import com.m.vodovoz.core.navigation.VodovozNavKey

data class OrderQuestionNavKey(
    val orderId: Long,
) : VodovozNavKey {
    companion object {
        const val NAV_NAME: String = "feature/order_question/OrderQuestion"
    }
}
