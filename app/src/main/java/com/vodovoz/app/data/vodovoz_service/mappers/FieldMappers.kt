package com.vodovoz.app.data.vodovoz_service.mappers

import com.vodovoz.app.data.vodovoz_service.model.FieldsDTO
import com.vodovoz.app.domain.general.model.user.ChangePasswordDetailsModel


fun FieldsDTO.toDomain(): ChangePasswordDetailsModel {
    return ChangePasswordDetailsModel(
        title = TITLE ?: "",
        fields = POLYA?.mapToDomain() ?: throw IllegalArgumentException("Change password fields can't be null")
    )
}