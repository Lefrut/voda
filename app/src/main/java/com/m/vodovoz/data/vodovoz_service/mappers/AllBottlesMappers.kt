package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.data.vodovoz_service.model.AllBottlesDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.TARA_DTO
import com.m.vodovoz.domain.general.model.product.AllBottlesDetailsModel
import com.m.vodovoz.domain.general.model.product.BottleModel

fun AllBottlesDetailsDTO.toDomain(): AllBottlesDetailsModel {
    return AllBottlesDetailsModel(
        description = OPISANIE ?: "",
        isSingleBottleMode = KPOPKAPLUS != "Y",
        bottles = TARA?.mapToDomain() ?: emptyList()
    )
}

fun TARA_DTO.toDomain(): BottleModel?{
    return BottleModel(
        name = NAME ?: "",
        id = ID ?: return null,
        articleText = PROPERTY_CML2_ARTICLE_VALUE ?: "",
        description = OPISANIE ?: "",
        cartQuantity = 0
    )
}

fun List<TARA_DTO>.mapToDomain(): List<BottleModel>{
    return mapNotNull { it.toDomain() }
}