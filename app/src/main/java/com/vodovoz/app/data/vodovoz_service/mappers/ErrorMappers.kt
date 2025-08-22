package com.vodovoz.app.data.vodovoz_service.mappers

import com.vodovoz.app.common.model.VodovozBoolean
import com.vodovoz.app.common.model.boolean
import com.vodovoz.app.common.model.from
import com.vodovoz.app.data.vodovoz_service.di.toVodovozUrl
import com.vodovoz.app.data.vodovoz_service.model.VodovozButtonDTO
import com.vodovoz.app.data.vodovoz_service.model.VodovozPlaceholderDTO
import com.vodovoz.app.domain.general.model.exceptions.VodovozPlaceholderModel
import com.vodovoz.app.domain.general.model.promotion.ColorfulButtonModel


fun VodovozPlaceholderDTO.toDomain(): VodovozPlaceholderModel {
    return VodovozPlaceholderModel(
        headerHtml = header ?: "",
        descriptionHtml = message ?: "",
        imageUrl = imageUrl?.toVodovozUrl() ?: "",
        title = title ?: "",
        button = button?.toDomain()
    )
}

fun VodovozButtonDTO.toDomain(): ColorfulButtonModel {
    return ColorfulButtonModel(
        name = text ?: "",
        backgroundColor = background ?: "",
        textColor = color ?: "",
        id = id ?: "",
        browser = browser?.let { browser ->
            VodovozBoolean.from(browser).boolean
        },
        url = url
    )
}
