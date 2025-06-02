package com.vodovoz.app.data.vodovoz_service.mappers

import com.vodovoz.app.data.vodovoz_service.model.auth.AuthDetailsDTO
import com.vodovoz.app.data.vodovoz_service.model.auth.KNOPKA_AUTH_DTO
import com.vodovoz.app.data.vodovoz_service.model.auth.LoginByPhoneDTO
import com.vodovoz.app.data.vodovoz_service.model.auth.RequestCodeDTO
import com.vodovoz.app.data.vodovoz_service.model.auth.UserAuthInfoDTO
import com.vodovoz.app.domain.general.model.ColorfulButtonModel
import com.vodovoz.app.domain.general.model.login.AuthDetailsModel
import com.vodovoz.app.domain.general.model.login.RequestCodeModel
import com.vodovoz.app.domain.general.model.login.UserAuthInfoModel
import java.time.Duration
import java.time.LocalDateTime

fun AuthDetailsDTO.toDomain(): AuthDetailsModel {
    return AuthDetailsModel(
        title = TITLE ?: "",
        description = OPISANIE ?: "",
        fields = DATA?.mapToDomain()
            ?: throw IllegalArgumentException("Login fields can't be null"),
        haveAgreement = SOGLASHENIE == "Y",
        buttons = KNOPKA?.map { it.toDomain() }
            ?: throw IllegalArgumentException("Auth button can't be null")
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

fun RequestCodeDTO.toDomain(): RequestCodeModel{
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

fun LoginByPhoneDTO.toDomain(): UserAuthInfoModel{
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