package com.vodovoz.app.data.vodovoz_service.mappers

import com.vodovoz.app.common.model.VodovozBoolean
import com.vodovoz.app.common.model.boolean
import com.vodovoz.app.common.model.from
import com.vodovoz.app.data.vodovoz_service.model.CHECKBOX_DTO
import com.vodovoz.app.data.vodovoz_service.model.auth.AuthDetailsDTO
import com.vodovoz.app.data.vodovoz_service.model.auth.KNOPKA_AUTH_DTO
import com.vodovoz.app.data.vodovoz_service.model.auth.LoginByPhoneDTO
import com.vodovoz.app.data.vodovoz_service.model.auth.RequestCodeDTO
import com.vodovoz.app.data.vodovoz_service.model.auth.UserAuthInfoDTO
import com.vodovoz.app.domain.general.model.CheckBoxModel
import com.vodovoz.app.domain.general.model.promotion.ColorfulButtonModel
import com.vodovoz.app.domain.general.model.user.AuthDetailsModel
import com.vodovoz.app.domain.general.model.user.RequestCodeModel
import com.vodovoz.app.domain.general.model.user.UserAuthInfoModel
import java.time.Duration
import java.time.LocalDateTime

fun AuthDetailsDTO.toDomain(): AuthDetailsModel {
    return AuthDetailsModel(
        title = TITLE ?: "",
        description = OPISANIE ?: "",
        fields = DATA?.mapToDomain()
            ?: throw IllegalArgumentException("Login fields can't be null"),
        showAgreement = VodovozBoolean.from(SOGLASHENIE).boolean,
        buttons = KNOPKA?.map { it.toDomain() }
            ?: throw IllegalArgumentException("Auth button can't be null"),
        checkboxes = PODPISKA?.mapToDomain() ?: emptyList()
    )
}

fun List<CHECKBOX_DTO>.mapToDomain(): List<CheckBoxModel> {
    return mapNotNull { it.toDomain() }
}

fun CHECKBOX_DTO.toDomain(): CheckBoxModel? {
    return CheckBoxModel(
        isRequired = VodovozBoolean.from(OBYAZATELNO).boolean,
        name = NAME ?: "",
        checked = VodovozBoolean.from(VALUE).boolean,
        id = ID ?: return null
    )
}

fun KNOPKA_AUTH_DTO.toDomain(): ColorfulButtonModel {
    return ColorfulButtonModel(
        name = TITLE ?: NAME ?: "",
        backgroundColor = BACKGROUND ?: BACGROUND ?: "",
        textColor = TEXTCOLOR ?: "",
        id = ID ?: ""
    )
}

fun RequestCodeDTO.toDomain(): RequestCodeModel {
    val wait = time?.toIntOrNull() ?: 60
    val now = LocalDateTime.now()
    val start = data ?: now

    val elapsed = Duration.between(start, now).seconds.toInt()
    val remaining = (wait - elapsed).coerceAtLeast(0)

    return RequestCodeModel(
        waitSeconds = wait,
        remainingSeconds = remaining
    )
}

fun LoginByPhoneDTO.toDomain(): UserAuthInfoModel {
    return UserAuthInfoModel(
        userId = DATA?.userId ?: throw IllegalArgumentException("userId is required"),
        authStatus = DATA.authStatus ?: true,
        token = TOKEN ?: throw IllegalArgumentException("token is required")
    )
}

fun UserAuthInfoDTO.toDomain(): UserAuthInfoModel {
    return UserAuthInfoModel(
        userId = userId ?: userId2 ?: throw IllegalArgumentException("userId is required"),
        authStatus = authStatus ?: true,
        token = token ?: throw IllegalArgumentException("token is required")
    )
}