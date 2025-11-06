package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.data.vodovoz_service.di.toVodovozUrl
import com.m.vodovoz.data.vodovoz_service.model.APP_UPDATE_INFO_DTO
import com.m.vodovoz.data.vodovoz_service.model.PopupWindowDTO
import com.m.vodovoz.data.vodovoz_service.model.SPECTIAL_PROMOTION_DTO
import com.m.vodovoz.domain.general.model.promotion.AppUpdateInfoModel
import com.m.vodovoz.domain.general.model.promotion.PopupWindowInfoModel
import com.m.vodovoz.domain.general.model.promotion.SpecialPromotionModel

fun PopupWindowDTO.toDomain(): PopupWindowInfoModel {
    return PopupWindowInfoModel(
        specialPromotion = BANNER?.firstOrNull()?.toDomain(),
        appUpdateInfo = UPDATE!!.toDomain()
    )
}


fun SPECTIAL_PROMOTION_DTO.toDomain(): SpecialPromotionModel {
    return SpecialPromotionModel(
        id = ID ?: -1,
        name = NAME ?: "",
        text = TEXT ?: "",
        picture = KARTINKA?.toVodovozUrl() ?: "",
        actionWithButton = HARAKTERISTIK?.toDomain(),
        aboutAdvertising = OREKLAME?.toDomain()
    )
}

fun APP_UPDATE_INFO_DTO.toDomain(): AppUpdateInfoModel {
    return AppUpdateInfoModel(
        id = ID ?: -1,
        title = TITLE ?: "",
        text = TEXT ?: "",
        playMarketUrl = SILKA_ANDROID ?: "",
        androidVersion = VERSIYA_ANDROID.toString(),
        picture = KARTINKA?.toVodovozUrl() ?: "",
        colorfulButton = HARAKTERISTIK?.KNOPKA?.toDomain()
    )
}