package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.data.vodovoz_service.di.toVodovozUrl
import com.m.vodovoz.data.vodovoz_service.model.unrated_products.UnratedProductDTO
import com.m.vodovoz.data.vodovoz_service.model.unrated_products.UnratedProductsSectionDTO
import com.m.vodovoz.domain.general.model.exceptions.EmptyResultException
import com.m.vodovoz.domain.general.model.product.UnratedProductModel
import com.m.vodovoz.domain.general.model.product.UnratedProductsSectionModel

fun UnratedProductsSectionDTO.toDomain(): UnratedProductsSectionModel {
    return UnratedProductsSectionModel(
        title = TITLERAZDEL ?: "",
        products = LISTRAZDEL?.mapNotNull { it.toDomain() }
            ?: throw EmptyResultException("Unrated products are empty"),
        productTitle = TITLETOVAR ?: "",
        countProductsText = VSEGOTOVAR ?: "",
        buttonText = KNOPKA?.NAME ?: ""
    )
}

fun UnratedProductDTO.toDomain(): UnratedProductModel? {
    return UnratedProductModel(
        id = ID ?: return null,
        name = NAME ?: return null,
        detailPicture = DETAIL_PICTURE?.toVodovozUrl() ?: return null
    )
}