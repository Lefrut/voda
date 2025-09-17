package com.m.vodovoz.domain.general.model.user

import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel
import com.m.vodovoz.domain.general.model.product.SectionModel

data class NotificationSettingsDetailsModel(
    val title: String,
    val sections: List<SectionModel<NotificationSectionItemModel>>,
    val button: ColorfulButtonModel
)
