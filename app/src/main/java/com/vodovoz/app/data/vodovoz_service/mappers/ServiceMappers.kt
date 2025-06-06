package com.vodovoz.app.data.vodovoz_service.mappers

import com.vodovoz.app.data.vodovoz_service.di.toVodovozUrl
import com.vodovoz.app.data.vodovoz_service.model.services.AllServicesDetailsDTO
import com.vodovoz.app.data.vodovoz_service.model.services.SERVICE_DETAILS_BUTTON_DTO
import com.vodovoz.app.data.vodovoz_service.model.services.SERVICE_DTO
import com.vodovoz.app.data.vodovoz_service.model.services.ServiceDetailsDTO
import com.vodovoz.app.data.vodovoz_service.model.services.ServiceOrderDetailsDTO
import com.vodovoz.app.data.vodovoz_service.model.services.ServiceProductsDTO
import com.vodovoz.app.domain.general.model.ColorfulButtonModel
import com.vodovoz.app.domain.general.model.service.AllServicesDetailsModel
import com.vodovoz.app.domain.general.model.service.ServiceDetailsModel
import com.vodovoz.app.domain.general.model.service.ServiceModel
import com.vodovoz.app.domain.general.model.service.ServiceOrderDetailsModel
import com.vodovoz.app.domain.general.model.service.ServiceProductsModel


fun ServiceOrderDetailsDTO.toDomain(): ServiceOrderDetailsModel{
    return ServiceOrderDetailsModel(
        title = TITLE ?: "",
        subtitle = INFORMIROVANIE ?: "",
        fields = LISTADATA?.mapToDomain() ?: throw IllegalArgumentException("ServiceOrderDetails fields can't be null"),
        button =  KNOPKA?.toDomain() ?: throw IllegalArgumentException("ServiceOrderDetails button can't be null")
    )
}

fun AllServicesDetailsDTO.toDomain(): AllServicesDetailsModel{
    return AllServicesDetailsModel(
        title = TITLE ?: "",
        description = NAME ?: "",
        services = OPIS?.mapToDomain() ?: emptyList()
    )
}

fun List<SERVICE_DTO>.mapToDomain(): List<ServiceModel>{
    return mapNotNull { it.toDomain() }
}

fun SERVICE_DTO.toDomain(): ServiceModel? {
    return ServiceModel(
        id = ID ?: return null,
        name = NAME ?: return null,
        image = PREVIEW_PICTURE?.toVodovozUrl() ?: return null
    )
}

fun ServiceDetailsDTO.toDomain(): ServiceDetailsModel{
    return ServiceDetailsModel(
        id = ID ?: throw IllegalArgumentException("Service id can't be null"),
        name = NAME ?: "",
        html = DETAIL_TEXT ?: "",
        image = PREVIEW_PICTURE?.toVodovozUrl() ?: "",
        productsSection = TOVAR?.toDomain(),
        button = KNOPKA?.toDomain()
    )
}

fun SERVICE_DETAILS_BUTTON_DTO.toDomain(): ColorfulButtonModel{
    return ColorfulButtonModel(
        name = NAME ?: "",
        backgroundColor = BACKGROUND ?: "",
        textColor = TEXTCOLOR ?: "",
        id = ID ?: ""
    )
}

fun ServiceProductsDTO.toDomain(): ServiceProductsModel{
    return ServiceProductsModel(
        title = TITLE ?: "",
        coefficient = KOEFFICIENT ?: 1,
        products = TOVARY?.mapToDomain() ?: emptyList(),
        additionalProductId =  DOPTOVAR ?: ""
    )
}

