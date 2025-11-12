package com.m.vodovoz.data.vodovoz_service.mappers

import androidx.core.text.HtmlCompat
import com.m.vodovoz.data.vodovoz_service.di.toVodovozUrl
import com.m.vodovoz.data.vodovoz_service.model.AKCIYA_DTO
import com.m.vodovoz.data.vodovoz_service.model.HIT_DTO
import com.m.vodovoz.data.vodovoz_service.model.OREKLAME_DTO
import com.m.vodovoz.data.vodovoz_service.model.PROMOTION_DATA_DTO
import com.m.vodovoz.data.vodovoz_service.model.PROMOTION_RAZDEL_DTO
import com.m.vodovoz.data.vodovoz_service.model.PromotionsDTO
import com.m.vodovoz.domain.general.model.promotion.AboutAdvertisingModel
import com.m.vodovoz.domain.general.model.widgets.LabelModel
import com.m.vodovoz.domain.general.model.promotion.PromotionCategoryModel
import com.m.vodovoz.domain.general.model.promotion.PromotionDetailsModel
import com.m.vodovoz.domain.general.model.promotion.PromotionModel
import com.m.vodovoz.domain.general.model.promotion.PromotionsSectionModel

fun AKCIYA_DTO.toDomain(): PromotionDetailsModel? {
    return PromotionDetailsModel(
        id = ID ?: return null,
        picture = DETAIL_PICTURE?.toVodovozUrl() ?: "",
        name = NAME ?: "",
        description = DETAIL_TEXT ?: "",
        endDate = DATAOUT?.let { mapToZonedDateTime(DATAOUT) },
        advertising = OREKLAME?.toDomain(),
        label = HIT?.toDomain()
    )
}

fun PromotionsDTO.toDomain(): PromotionsSectionModel {
    return PromotionsSectionModel(
        title = TITLE ?: "",
        categories = RAZDELI?.mapToDomain() ?: emptyList(),
        promotions = DATA?.mapToDomain() ?: emptyList(),
        button = KNOPKA?.toDomain()
    )

}

@JvmName("mapPromotionRazdelToDomain")
fun List<PROMOTION_RAZDEL_DTO>.mapToDomain(): List<PromotionCategoryModel> {
    return mapNotNull { promotionRazdelDto ->
        promotionRazdelDto.toDomain()
    }
}

fun PROMOTION_RAZDEL_DTO.toDomain(): PromotionCategoryModel? {
    return PromotionCategoryModel(
        id = ID ?: return null,
        name = NAME ?: return null,
        code = CODE ?: return null
    )
}

fun List<PROMOTION_DATA_DTO>.mapToDomain(): List<PromotionModel> {
    return mapNotNull { promotionDataDto ->
        promotionDataDto.toDomain()
    }
}


fun PROMOTION_DATA_DTO.toDomain(): PromotionModel? {
    return PromotionModel(
        id = ID ?: return null,
        name = NAME ?: "",
        blockId = IBLOCK_ID ?: -1,
        sectionId = IBLOCK_SECTION_ID ?: -1,
        detailPicture = DETAIL_PICTURE?.toVodovozUrl() ?: "",
        endDate = mapToZonedDateTime(DATA_OUT ?: ""),
        label = HIT?.toDomain(),
        advertising = OREKLAME?.toDomain()
    )
}

fun HIT_DTO.toDomain(): LabelModel? {
    return LabelModel(
        name = TITLE ?: return null,
        backgroundColor = BACKGROUND ?: return null,
        textColor = "",
    )
}

fun OREKLAME_DTO.toDomain(): AboutAdvertisingModel? {
    val dannye = DANNYE ?: return null
    return AboutAdvertisingModel(
        name = this.NAME ?: "",
        title = this.ZAGOLOVOK ?: "",
        aboutCompanyTitle = this.NAMEVNUTRI ?: "",
        aboutCompany = HtmlCompat.fromHtml(dannye, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
    )
}