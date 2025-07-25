package com.vodovoz.app.domain.general.model

data class CheckBoxModel(
    val isRequired: Boolean,
    val name: String,
    val checked: Boolean,
    val id: String,
)
