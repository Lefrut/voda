package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.data.vodovoz_service.model.FieldsDTO
import com.m.vodovoz.domain.general.model.user.ChangePasswordDetailsModel


fun FieldsDTO.toDomain(): ChangePasswordDetailsModel {
    return ChangePasswordDetailsModel(
        title = TITLE ?: "",
        fields = POLYA?.mapToDomain() ?: throw IllegalArgumentException("Change password fields can't be null")
    )
}