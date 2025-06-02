package com.vodovoz.app.domain.general.model

data class FieldModel(
    val id: String,
    val label: String,
    val value: String,
    val valueType: String,
    val isRequired: Boolean,
    val readOnly: Boolean,
    val supportingText: String,
    val hint: String,
)

fun List<FieldModel>.toQueries(): Map<String, String> {
    return filter { fieldModel -> fieldModel.value.isNotEmpty() }
        .associate { field -> field.id to field.value.trim() }
}

