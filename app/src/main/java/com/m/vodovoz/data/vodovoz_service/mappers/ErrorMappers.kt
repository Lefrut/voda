package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.common.model.VodovozBoolean
import com.m.vodovoz.common.model.boolean
import com.m.vodovoz.common.model.from
import com.m.vodovoz.data.vodovoz_service.di.toVodovozUrl
import com.m.vodovoz.data.vodovoz_service.model.VodovozButtonDTO
import com.m.vodovoz.data.vodovoz_service.model.VodovozPlaceholderDTO
import com.m.vodovoz.domain.general.model.exceptions.VodovozPlaceholderModel
import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel


fun VodovozPlaceholderDTO.toDomain(): VodovozPlaceholderModel {
    return VodovozPlaceholderModel(
        headerHtml = header ?: "",
        descriptionHtml = message ?: "",
        imageUrl = imageUrl?.toVodovozUrl() ?: "",
        title = title ?: "",
        button = button?.toDomain(),
        productsSection = productsSection?.toDomain()
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
