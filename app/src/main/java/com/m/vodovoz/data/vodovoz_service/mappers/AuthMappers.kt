package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.common.model.VodovozBoolean
import com.m.vodovoz.common.model.boolean
import com.m.vodovoz.common.model.from
import com.m.vodovoz.data.vodovoz_service.model.CHECKBOX_DTO
import com.m.vodovoz.data.vodovoz_service.model.auth.AccountTypeSectionDTO
import com.m.vodovoz.data.vodovoz_service.model.auth.AuthDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.auth.KNOPKA_AUTH_DTO
import com.m.vodovoz.data.vodovoz_service.model.auth.LoginByPhoneDTO
import com.m.vodovoz.data.vodovoz_service.model.auth.RequestCodeDTO
import com.m.vodovoz.data.vodovoz_service.model.auth.UserAuthInfoDTO
import com.m.vodovoz.domain.general.model.product.SectionModel
import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel
import com.m.vodovoz.domain.general.model.user.AuthDetailsModel
import com.m.vodovoz.domain.general.model.user.RequestCodeModel
import com.m.vodovoz.domain.general.model.user.UserAuthInfoModel
import com.m.vodovoz.domain.general.model.widgets.CheckboxModel
import com.m.vodovoz.domain.general.model.widgets.SwitchModel
import java.time.Duration
import java.time.LocalDateTime

fun AuthDetailsDTO.toDomain(): AuthDetailsModel {


    val agreementCheckbox = PODOFERTA?.toDomain()

    val checkboxes = buildList {
        agreementCheckbox?.let { add(agreementCheckbox) }
        addAll(PODPISKA?.mapToDomain() ?: emptyList())
    }

    return AuthDetailsModel(
        title = TITLE ?: "",
        description = OPISANIE ?: "",
        fields = DATA?.mapToDomain()
            ?: throw IllegalArgumentException("Login fields can't be null"),
        agreementCheckboxId = agreementCheckbox?.id,
        buttons = KNOPKA?.map { it.toDomain() }
            ?: throw IllegalArgumentException("Auth button can't be null"),
        checkboxes = checkboxes,
        accountTypeSection = BIZNES.toDomain()
    )
}


fun AccountTypeSectionDTO?.toDomain(): SectionModel<SwitchModel> {
    if (this == null) return SectionModel.empty()
    return SectionModel(
        title = TITLE.orEmpty(),
        items = CHEKBOX.orEmpty().mapToDomain()
    )
}

fun List<CHECKBOX_DTO>.mapToDomain(): List<CheckboxModel> {
    return mapNotNull { it.toDomain() }
}

fun CHECKBOX_DTO.toDomain(): CheckboxModel? {
    return CheckboxModel(
        isRequired = VodovozBoolean.from(OBYAZATELNO).boolean,
        name = NAME ?: TEXT ?: "",
        checked = (VALUE as? Boolean) ?: VodovozBoolean.from(VALUE.toString()).boolean,
        id = ID ?: return null,
        urlTitles = ZAGOLOVOKi ?: emptyList()
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