package com.vodovoz.app.domain.general.model.user

import com.vodovoz.app.domain.general.model.FieldModel

data class ChangePasswordDetailsModel(
    val title: String,
    val fields: List<FieldModel>,
)