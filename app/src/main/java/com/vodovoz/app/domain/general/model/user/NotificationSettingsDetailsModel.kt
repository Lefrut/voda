package com.vodovoz.app.domain.general.model.user

import com.vodovoz.app.domain.general.model.promotion.ColorfulButtonModel
import com.vodovoz.app.domain.general.model.product.SectionModel

data class NotificationSettingsDetailsModel(
    val title: String,
    val sections: List<SectionModel<NotificationSectionItemModel>>,
    val button: ColorfulButtonModel
)
