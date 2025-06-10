package com.vodovoz.app.domain.general.model.user

import com.vodovoz.app.domain.general.model.FieldModel

data class UserDataModel(
    val title: String,
    val photo: UserDataPhotoModel,
    val fields: List<FieldModel>,
)

data class UserDataPhotoModel(
    val imageUrl: String,
    val title: String,
    val description: String,
)
