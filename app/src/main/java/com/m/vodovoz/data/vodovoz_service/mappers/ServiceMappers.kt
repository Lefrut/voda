package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.data.vodovoz_service.di.toVodovozUrl
import com.m.vodovoz.data.vodovoz_service.model.services.AllServicesDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.services.SERVICE_DETAILS_BUTTON_DTO
import com.m.vodovoz.data.vodovoz_service.model.services.SERVICE_DTO
import com.m.vodovoz.data.vodovoz_service.model.services.ServiceDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.services.ServiceOrderDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.services.ServiceProductsDTO
import com.m.vodovoz.domain.general.model.order.FormModel
import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel
import com.m.vodovoz.domain.general.model.service.AllServicesDetailsModel
import com.m.vodovoz.domain.general.model.service.ServiceDetailsModel
import com.m.vodovoz.domain.general.model.service.ServiceModel
import com.m.vodovoz.domain.general.model.service.ServiceProductsModel


fun ServiceOrderDetailsDTO.toDomain(): FormModel{
    return FormModel(
        title = TITLE ?: "",
        description = INFORMIROVANIE ?: "",
        fields = LISTADATA?.mapToDomain() ?: throw IllegalArgumentException("ServiceOrderDetails fields can't be null"),
        checkbox = PODOFERTA?.toDomain(),
        button = KNOPKA?.toDomain() ?: throw IllegalArgumentException("ServiceOrderDetails button can't be null")
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

fun SERVICE_DETAILS_BUTTON_DTO.toDomain(): ColorfulButtonModel {
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

