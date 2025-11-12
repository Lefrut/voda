package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.data.vodovoz_service.model.LABEL_DTO
import com.m.vodovoz.domain.general.model.widgets.LabelModel


fun LABEL_DTO.toDomain(): LabelModel? {
    return LabelModel(
        name = name ?: return null,
        backgroundAlpha = backgroundAlpha ?: 1f,
        backgroundColor = backgroundColor ?: "",
        textColor = textColor ?: ""

    )
}