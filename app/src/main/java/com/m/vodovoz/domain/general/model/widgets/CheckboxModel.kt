package com.m.vodovoz.domain.general.model.widgets

data class CheckboxModel(
    val isRequired: Boolean,
    val name: String,
    val checked: Boolean,
    val urlTitles: List<String>,
    val id: String,
)
