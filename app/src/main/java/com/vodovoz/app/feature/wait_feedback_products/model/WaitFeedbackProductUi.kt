package com.vodovoz.app.feature.wait_feedback_products.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.domain.general.model.product.WaitFeedbackProductModel

@Immutable
data class WaitFeedbackProductUi(
    val id: Long,
    val name: String,
    val image: String
)

fun WaitFeedbackProductModel.toUi(): WaitFeedbackProductUi{
    return WaitFeedbackProductUi(id, name, image)
}
