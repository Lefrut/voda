package com.vodovoz.app.data.vodovoz_service.mappers

import android.text.Html
import androidx.core.text.HtmlCompat
import com.vodovoz.app.data.vodovoz_service.di.toVodovozUrl
import com.vodovoz.app.data.vodovoz_service.model.AKCIYA_DTO
import com.vodovoz.app.data.vodovoz_service.model.HIT_DTO
import com.vodovoz.app.data.vodovoz_service.model.OREKLAME_DTO
import com.vodovoz.app.data.vodovoz_service.model.PROMOTION_DATA_DTO
import com.vodovoz.app.data.vodovoz_service.model.PROMOTION_RAZDEL_DTO
import com.vodovoz.app.data.vodovoz_service.model.PromotionsDTO
import com.vodovoz.app.domain.general.model.promotion.AboutAdvertisingModel
import com.vodovoz.app.domain.general.model.promotion.LabelModel
import com.vodovoz.app.domain.general.model.promotion.PromotionCategoryModel
import com.vodovoz.app.domain.general.model.promotion.PromotionDetailsModel
import com.vodovoz.app.domain.general.model.promotion.PromotionModel
import com.vodovoz.app.domain.general.model.promotion.PromotionsSectionModel

fun AKCIYA_DTO.toDomain(): PromotionDetailsModel? {
    return PromotionDetailsModel(
        id = ID ?: return null,
        picture = DETAIL_PICTURE?.toVodovozUrl() ?: return null,
        name = NAME ?: return null,
        description = DETAIL_TEXT ?: "",
        endDate = mapToZonedDateTime(DATAOUT ?: return null) ?: return null,
        advertising = OREKLAME?.toDomain(),
        label = HIT?.toDomain()
    )
}

fun PromotionsDTO.toDomain(): PromotionsSectionModel {
    return PromotionsSectionModel(
        title = TITLE ?: "",
        categories = RAZDELI?.filterNotNull()?.toDomain() ?: emptyList(),
        promotions = DATA?.toDomain() ?: emptyList(),
        button = KNOPKA?.toDomain()
    )

}

@JvmName("mapPromotionRazdelToDomain")
fun List<PROMOTION_RAZDEL_DTO>.toDomain(): List<PromotionCategoryModel> {
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

fun List<PROMOTION_DATA_DTO?>.toDomain(): List<PromotionModel> {
    return mapNotNull { promotionDataDto ->
        promotionDataDto?.toDomain()
    }
}


fun PROMOTION_DATA_DTO.toDomain(): PromotionModel? {
    return PromotionModel(
        id = ID ?: return null,
        name = NAME ?: return null,
        blockId = IBLOCK_ID ?: -1,
        sectionId = IBLOCK_SECTION_ID ?: -1,
        detailPicture = DETAIL_PICTURE?.toVodovozUrl() ?: return null,
        endDate = mapToZonedDateTime(DATA_OUT ?: return null) ?: return null,
        label = HIT?.toDomain() ?: LabelModel.Empty,
        advertising = OREKLAME?.toDomain()
    )
}

fun HIT_DTO.toDomain(): LabelModel? {
    return LabelModel(
        name = TITLE ?: return null,
        colorHex = BACKGROUND ?: return null
    )
}

fun OREKLAME_DTO.toDomain(): AboutAdvertisingModel? {
    val dannye = DANNYE ?: return null
    return AboutAdvertisingModel(
        name = this.NAME ?: "",
        title = this.ZAGOLOVOK ?: return null,
        aboutCompanyTitle = this.NAMEVNUTRI ?: return null,
        aboutCompany = HtmlCompat.fromHtml(dannye, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
    )
}