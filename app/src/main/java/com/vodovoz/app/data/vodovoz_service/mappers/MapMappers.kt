package com.vodovoz.app.data.vodovoz_service.mappers

import com.vodovoz.app.data.vodovoz_service.di.toVodovozUrl
import com.vodovoz.app.data.vodovoz_service.model.order.IMAGE_AND_TEXT_DTO
import com.vodovoz.app.data.vodovoz_service.model.order.TOCHKA_DTO
import com.vodovoz.app.data.vodovoz_service.model.order.WHERE_ORDER_BUTTON_DTO
import com.vodovoz.app.data.vodovoz_service.model.order.WhereMyOrderDetailsDTO
import com.vodovoz.app.domain.general.model.widgets.ImageAndTextModel
import com.vodovoz.app.domain.general.model.widgets.ImageButtonModel
import com.vodovoz.app.domain.general.model.location.MapPointModel
import com.vodovoz.app.domain.general.model.order.WhereOrderDetailsModel

fun WhereMyOrderDetailsDTO.toDomain(): WhereOrderDetailsModel {
    return WhereOrderDetailsModel(
        title = TITLE ?: "",
        secondTitle = VODITEL?.TITLE ?: "",
        description = OPISANIE ?: "",
        finishPoint = KLIENT?.TOCHKA?.toDomain(),
        driverPont = VODITEL?.TOCHKA?.toDomain(),
        buttons = VODITEL?.KNOPKI?.mapToDomain() ?: emptyList(),
        items = VODITEL?.DANNYE?.mapToDomain() ?: emptyList(),
    )
}

@JvmName("mapToImageAndTextModelList")
fun List<IMAGE_AND_TEXT_DTO>.mapToDomain(): List<ImageAndTextModel> {
    return mapNotNull { it.toDomain() }
}

fun IMAGE_AND_TEXT_DTO.toDomain(): ImageAndTextModel? {
    return ImageAndTextModel(
        text = POLE ?: TEXT ?: return null,
        image = KARTINKA?.toVodovozUrl() ?: return null
    )
}

@JvmName("mapToImageButtonModelList")
fun List<WHERE_ORDER_BUTTON_DTO>.mapToDomain(): List<ImageButtonModel> {
    return mapNotNull { it.toDomain() }
}

fun WHERE_ORDER_BUTTON_DTO.toDomain(): ImageButtonModel? {
    return ImageButtonModel(
        name = NAME ?: "",
        backgroundColor = BACKGROUND ?: "",
        textColor = COLOR ?: "",
        id = ID ?: return null,
        image = IMAGE?.toVodovozUrl() ?: ""
    )
}

fun TOCHKA_DTO.toDomain(): MapPointModel? {
    return MapPointModel(
        lat = Latitude ?: return null,
        lon = Longitude ?: return null
    )
}
