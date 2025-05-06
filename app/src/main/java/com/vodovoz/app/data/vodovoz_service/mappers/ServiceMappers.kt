package com.vodovoz.app.data.vodovoz_service.mappers

import com.vodovoz.app.data.vodovoz_service.di.toFullUrl
import com.vodovoz.app.data.vodovoz_service.model.services.AllServicesDetailsDTO
import com.vodovoz.app.data.vodovoz_service.model.services.SERVICE_DTO
import com.vodovoz.app.domain.general.model.service.AllServicesDetailsModel
import com.vodovoz.app.domain.general.model.service.ServiceModel

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
        image = PREVIEW_PICTURE?.toFullUrl() ?: return null
    )
}