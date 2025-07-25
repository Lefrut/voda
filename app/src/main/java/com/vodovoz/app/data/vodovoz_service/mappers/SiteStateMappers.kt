package com.vodovoz.app.data.vodovoz_service.mappers

import com.vodovoz.app.common.model.AgreementModel
import com.vodovoz.app.common.model.JivoChatModel
import com.vodovoz.app.common.model.TrackingConfig
import com.vodovoz.app.common.model.VodovozBoolean
import com.vodovoz.app.common.model.VodovozSiteState
import com.vodovoz.app.common.model.VodovozSiteStateContact
import com.vodovoz.app.common.model.VodovozSiteStateData
import com.vodovoz.app.common.model.boolean
import com.vodovoz.app.common.model.from
import com.vodovoz.app.data.vodovoz_service.model.CHATJIVO_DTO
import com.vodovoz.app.data.vodovoz_service.model.GENERATION_DTO
import com.vodovoz.app.data.vodovoz_service.model.SITE_STATE_TRANSITION_DTO
import com.vodovoz.app.data.vodovoz_service.model.SOGLASHENIE_DTO
import com.vodovoz.app.data.vodovoz_service.model.SiteStateDataDTO
import com.vodovoz.app.data.vodovoz_service.model.SiteStateResponseDTO

fun SiteStateResponseDTO.toDomain(): VodovozSiteState {
    return VodovozSiteState(
        isActive = !VodovozBoolean.from(ACTIVE).boolean,
        testUrl = TESTSAITSSILKA ?: "",
        smsUrl = SMSRASSILKA ?: "",
        isSmsEnabled = VodovozBoolean.from(REGISTRACION_SMS).boolean,
        jivoChat = CHATJIVO?.toJivoChatModel() ?: JivoChatModel(isActive = false, url = ""),
        tracking = GENERATION?.toTrackingConfig() ?: TrackingConfig(
            trackingIsEnabled = false,
            time = 0
        ),
        agreement = SOGLASHENIE?.toAgreementModel() ?: AgreementModel("", emptyList()),
        takePhotos = COMMENTFILES ?: false,
        callPhoneNumber = CALL ?: "",
        data = DATA?.toDomain()
    )
}

fun SiteStateDataDTO.toDomain(): VodovozSiteStateData {
    return VodovozSiteStateData(
        title = TITLE ?: "",
        logo = LOGO ?: "",
        description = OPISANIE ?: "",
        email = EMAIL ?: "",
        time = TIME ?: "",
        phone = TELEFON ?: "",
        contacts = KLYCH?.mapNotNull { contact ->
            contact.toDomain()
        } ?: emptyList()
    )
}

fun SITE_STATE_TRANSITION_DTO.toDomain(): VodovozSiteStateContact? {
    return VodovozSiteStateContact(
        url = URL ?: "",
        urlType = TYPE ?: "",
        image = IMAGES ?: return null
    )
}


fun CHATJIVO_DTO.toJivoChatModel(): JivoChatModel {
    return JivoChatModel(
        isActive = VodovozBoolean.from(ACTIVE).boolean && SSILKA != null,
        url = SSILKA ?: ""
    )
}

fun GENERATION_DTO.toTrackingConfig(): TrackingConfig {
    return TrackingConfig(
        trackingIsEnabled = VodovozBoolean.from(TRAKING).boolean,
        time = TIME?.toInt() ?: 30
    )
}

fun SOGLASHENIE_DTO.toAgreementModel(): AgreementModel {
    return AgreementModel(
        html = TEXT ?: "",
        titles = ZAGOLOVOKi?.mapNotNull { it } ?: emptyList()
    )
}