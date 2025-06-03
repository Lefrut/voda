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
    val values: List<FieldOptionModel> = emptyList()
)

data class FieldOptionModel(
    val id: String,
    val value: String
)

fun List<FieldModel>.toQueries(): Map<String, String> {
    return filter { fieldModel -> fieldModel.value.isNotEmpty() }
        .associate { field ->
            val optionId = field.values.firstOrNull { it.value == field.value }?.id
            field.id to (optionId ?: field.value.trim())
        }
}

