package com.vodovoz.app.data.vodovoz_service.mappers

import com.vodovoz.app.data.vodovoz_service.model.CHATJIVO_DTO
import com.vodovoz.app.data.vodovoz_service.model.GENERATION_DTO
import com.vodovoz.app.data.vodovoz_service.model.SOGLASHENIE_DTO
import com.vodovoz.app.data.vodovoz_service.model.SiteStateResponseDTO
import com.vodovoz.app.common.model.AgreementModel
import com.vodovoz.app.common.model.JivoChatModel
import com.vodovoz.app.common.model.VodovozSiteState
import com.vodovoz.app.common.model.TrackingConfig

fun SiteStateResponseDTO.toDomain(): VodovozSiteState {
    return VodovozSiteState(
        //TODO - mb replace to "Y"
        isActive = ACTIVE == "N",
        testUrl = TESTSAITSSILKA ?: "",
        smsUrl = SMSRASSILKA ?: "",
        isSmsEnabled = REGISTRACION_SMS == "Y",
        jivoChat = CHATJIVO?.toJivoChatModel() ?: JivoChatModel(isActive = false, url = ""),
        tracking = GENERATION?.toTrackingConfig() ?: TrackingConfig(trackingIsEnabled = false, time = 0),
        agreement = SOGLASHENIE?.toAgreementModel() ?: throw IllegalArgumentException("Agreement can't be null"),
        takePhotos = COMMENTFILES ?: false,
        callPhoneNumber = CALL ?: ""
    )
}


fun CHATJIVO_DTO.toJivoChatModel(): JivoChatModel {
    return JivoChatModel(
        isActive = ACTIVE == "Y" && SSILKA != null,
        url = SSILKA ?: ""
    )
}

fun GENERATION_DTO.toTrackingConfig(): TrackingConfig {
    return TrackingConfig(
        trackingIsEnabled = TRAKING == "Y",
        time = TIME?.toInt() ?: 30
    )
}

fun SOGLASHENIE_DTO.toAgreementModel(): AgreementModel {
    return AgreementModel(
        html = TEXT ?: "",
        titles = ZAGOLOVOKi?.mapNotNull { it } ?: emptyList()
    )
}