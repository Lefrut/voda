package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.data.vodovoz_service.di.toVodovozUrl
import com.m.vodovoz.data.vodovoz_service.model.POPULAR_CATEGORY_DTO
import com.m.vodovoz.data.vodovoz_service.model.PopularCategoriesDTO
import com.m.vodovoz.domain.general.model.product.PopularCategoryModel
import com.m.vodovoz.domain.general.model.product.SectionModel

fun PopularCategoriesDTO.toDomain(): SectionModel<PopularCategoryModel> {
    return SectionModel(
        title = TITLERAZDEL ?: "",
        items = LISTRAZDEL?.mapNotNull { it?.toDomain() } ?: emptyList(),
        button = null
    )
}

fun POPULAR_CATEGORY_DTO.toDomain(): PopularCategoryModel? {
    return PopularCategoryModel(
        id = this.IDRAZDEL ?: return null,
        name = this.NAMERAZDEL ?: return null,
        picture = this.PICTURE?.toVodovozUrl() ?: return null,
        action = this.UF_SILKAPEREXOD?.toDataAllAction()
    )
}