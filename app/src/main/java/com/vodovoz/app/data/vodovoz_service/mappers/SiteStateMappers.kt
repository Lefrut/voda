package com.vodovoz.app.data.vodovoz_service.mappers

import com.vodovoz.app.common.constants.AppKeys
import com.vodovoz.app.common.model.Agreement
import com.vodovoz.app.common.model.AppConfig
import com.vodovoz.app.common.model.AppLink
import com.vodovoz.app.common.model.AppLinks
import com.vodovoz.app.common.model.BlockSiteContact
import com.vodovoz.app.common.model.BlockSiteInfo
import com.vodovoz.app.common.model.JivoChat
import com.vodovoz.app.common.model.TrackingInfo
import com.vodovoz.app.common.model.VodovozBoolean
import com.vodovoz.app.common.model.boolean
import com.vodovoz.app.common.model.from
import com.vodovoz.app.data.vodovoz_service.model.AppLinkDTO
import com.vodovoz.app.data.vodovoz_service.model.AppLinksDTO
import com.vodovoz.app.data.vodovoz_service.model.CHATJIVO_DTO
import com.vodovoz.app.data.vodovoz_service.model.GENERATION_DTO
import com.vodovoz.app.data.vodovoz_service.model.SITE_STATE_TRANSITION_DTO
import com.vodovoz.app.data.vodovoz_service.model.SOGLASHENIE_DTO
import com.vodovoz.app.data.vodovoz_service.model.SiteStateDataDTO
import com.vodovoz.app.data.vodovoz_service.model.SiteStateResponseDTO

fun SiteStateResponseDTO.toDomain(): AppConfig {
    val mapKeysAndroid = IDMAPKIT?.ANDROID

    return AppConfig(
        isActive = !VodovozBoolean.from(ACTIVE).boolean,
        testUrl = TESTSAITSSILKA ?: "",
        smsUrl = SMSRASSILKA ?: "",
        isSmsEnabled = VodovozBoolean.from(REGISTRACION_SMS).boolean,
        smsCodeCount = SMSPOLE ?: 4,
        jivoChat = CHATJIVO?.toJivoChatModel() ?: JivoChat(isActive = false, url = ""),
        tracking = GENERATION?.toTrackingConfig() ?: TrackingInfo(
            trackingIsEnabled = false,
            time = 0
        ),
        agreement = SOGLASHENIE?.toAgreementModel() ?: Agreement("", emptyList()),
        takePhotos = COMMENTFILES ?: false,
        callPhoneNumber = CALL ?: "",
        data = DATA?.toDomain(),
        mapkitKey = mapKeysAndroid?.MAPKIT ?: AppKeys.MAPKIT_API_KEY,
        geocoderKey = mapKeysAndroid?.GEOKODER ?: AppKeys.GEOCODER,
        appLinks = DANNYESSILKI?.toDomain() ?: AppLinks.Empty
    )
}

fun AppLinksDTO.toDomain(): AppLinks {
    return AppLinks(
        policy = politika.toDomain(),
        termsOfUse = oferta.toDomain(),
        aboutPayment = oplata.toDomain(),
        aboutDelivery = dostavka.toDomain(),
        personal = perdannie.toDomain()
    )
}

fun AppLinkDTO.toDomain(): AppLink {
    return AppLink(
        title = name,
        url = url
    )
}

fun SiteStateDataDTO.toDomain(): BlockSiteInfo {
    return BlockSiteInfo(
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

fun SITE_STATE_TRANSITION_DTO.toDomain(): BlockSiteContact? {
    return BlockSiteContact(
        url = URL ?: "",
        urlType = TYPE ?: "",
        image = IMAGES ?: return null
    )
}


fun CHATJIVO_DTO.toJivoChatModel(): JivoChat {
    return JivoChat(
        isActive = VodovozBoolean.from(ACTIVE).boolean && SSILKA != null,
        url = SSILKA ?: ""
    )
}

fun GENERATION_DTO.toTrackingConfig(): TrackingInfo {
    return TrackingInfo(
        trackingIsEnabled = VodovozBoolean.from(TRAKING).boolean,
        time = TIME?.toInt() ?: 30
    )
}

fun SOGLASHENIE_DTO.toAgreementModel(): Agreement {
    return Agreement(
        html = TEXT ?: "",
        titles = ZAGOLOVOKi?.mapNotNull { it } ?: emptyList()
    )
}