package com.m.vodovoz.domain.general.model.user

import com.m.vodovoz.domain.general.model.widgets.FieldModel

data class ChangePasswordDetailsModel(
    val title: String,
    val fields: List<FieldModel>,
)