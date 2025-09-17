package com.m.vodovoz.domain.general.model.user

import com.m.vodovoz.domain.general.model.widgets.FieldModel

data class UserDataModel(
    val title: String,
    val photo: UserDataPhotoModel,
    val fields: List<FieldModel>,
    val deleteTextPrefix: String,
    val deleteText: String
)

data class UserDataPhotoModel(
    val imageUrl: String,
    val title: String,
    val description: String,
)
