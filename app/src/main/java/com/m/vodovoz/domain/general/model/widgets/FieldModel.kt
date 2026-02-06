package com.m.vodovoz.domain.general.model.widgets

data class FieldModel(
    val id: String,
    val label: String,
    val value: String,
    val valueType: String,
    val isRequired: Boolean,
    val readOnly: Boolean,
    val supportingText: String,
    val hint: String,
    val values: List<FieldOptionModel> = emptyList(),
    val isVisible: Boolean = true
){
    companion object{
        const val CHANGE_ID = "oplata"
        const val BONUS_ID = "bonusoplata"

        const val COMMENT_ID = "comment"
    }
}

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

