package com.vodovoz.app.domain.general.model.order

import com.vodovoz.app.domain.general.model.promotion.ColorfulButtonModel

data class OrderCallYouDetailsModel(
    val title: String,
    val currentItem: CallYouItemModel,
    val items: List<CallYouItemModel>,
    val button: ColorfulButtonModel
)
