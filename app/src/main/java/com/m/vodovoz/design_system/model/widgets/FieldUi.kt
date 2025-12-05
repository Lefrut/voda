package com.m.vodovoz.design_system.model.widgets

import androidx.compose.runtime.Immutable
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.text.input.KeyboardType
import com.m.vodovoz.R
import com.m.vodovoz.domain.general.model.widgets.FieldModel
import com.m.vodovoz.domain.general.model.widgets.FieldOptionModel
import com.m.vodovoz.util.ValidationUtils
import com.m.vodovoz.util.ValidationUtils.PASSWORD_LENGTH
import com.m.vodovoz.util.isValidRussianPhoneNumber

enum class FieldValidationResult {
    VALID,
    INVALID,
    NOT_APPLICABLE;

    companion object {
        fun from(isValid: Boolean): FieldValidationResult {
            return if (isValid) VALID else INVALID
        }
    }
}


@Immutable
sealed interface FieldTypeUi {
    @Immutable
    data object Text : FieldTypeUi

    @Immutable
    data class DropDown(
        val options: List<DropDownOptionUi>,
    ) : FieldTypeUi
}

@Immutable
data class DropDownOptionUi(
    val id: String,
    val value: String,
)

@Immutable
data class FieldUi(
    override val id: String,
    val label: String,
    val value: String,
    val keyboardType: KeyboardType,
    val isRequired: Boolean,
    val isError: Boolean,
    val readOnly: Boolean,
    val supportingText: String,
    val hint: String = "",
    val type: FieldTypeUi,
    val isValueVisible: Boolean,
    val isVisible: Boolean = true,
    val contentType: ContentType? = null
) : WidgetUi(id) {
    companion object {
        val Empty = FieldUi(
            id = "",
            label = "",
            value = "",
            keyboardType = KeyboardType.Text,
            isRequired = false,
            isError = false,
            readOnly = false,
            supportingText = "",
            type = FieldTypeUi.Text,
            isValueVisible = true,
        )
    }

    override fun value(): String {
        return value
    }
}


fun interface FieldValidator {
    fun isValid(field: FieldUi): FieldValidationResult
}

val vodovozValidators
    get() = listOf(
        NoRequiredValidator,
        PhoneNumberValidator,
        EmailValidator,
        MessageValidator,
        NameValidator,
        INNValidator,
        EmptyTextValidator
    )

val EmptyTextValidator = FieldValidator { field ->
    return@FieldValidator when {
        field.value.isNotBlank() -> FieldValidationResult.VALID
        field.isRequired && field.value.isEmpty() -> FieldValidationResult.INVALID
        else -> FieldValidationResult.NOT_APPLICABLE
    }
}

val NoRequiredValidator = FieldValidator { field ->
    return@FieldValidator when {
        !field.isRequired && field.value.isBlank() && field.value.length < 500 -> FieldValidationResult.VALID
        else -> FieldValidationResult.NOT_APPLICABLE
    }
}

val PhoneNumberValidator = FieldValidator { field ->
    return@FieldValidator when (field.keyboardType) {
        KeyboardType.Phone -> FieldValidationResult.from(field.value.isValidRussianPhoneNumber())

        else -> FieldValidationResult.NOT_APPLICABLE
    }
}

val INNValidator = FieldValidator { field ->
    return@FieldValidator when (field.id) {
        "inn" -> FieldValidationResult.from(field.value.filter { char -> char.isDigit() }.length in 10..12)
        else -> FieldValidationResult.NOT_APPLICABLE
    }
}

val EmailValidator = FieldValidator { field ->
    return@FieldValidator when (field.keyboardType) {
        KeyboardType.Email -> FieldValidationResult.from(ValidationUtils.EMAIL_REGEX.matches(field.value))
        else -> FieldValidationResult.NOT_APPLICABLE
    }
}

val KeyboardTypeValidator = FieldValidator { field ->
    val value = field.value

    return@FieldValidator when (field.keyboardType) {
        KeyboardType.Text -> {
            FieldValidationResult.from(value.length in 2..100 && value.isNotBlank())
        }

        KeyboardType.Phone -> {
            FieldValidationResult.from(value.isValidRussianPhoneNumber())
        }

        KeyboardType.Email -> {
            FieldValidationResult.from(ValidationUtils.EMAIL_REGEX.matches(value))
        }

        KeyboardType.Password -> {
            FieldValidationResult.from(value.length in PASSWORD_LENGTH)
        }

        else -> FieldValidationResult.NOT_APPLICABLE
    }
}

val NameValidator = FieldValidator { field ->
    val value = field.value
    when {
        field.id == "name" || field.id == "lastname" || field.id == "dr49" || field.id == "fio" -> {
            FieldValidationResult.from(value.length in 3..30 && value.isNotBlank())
        }

        else -> FieldValidationResult.NOT_APPLICABLE
    }
}

val MessageValidator = FieldValidator { field ->
    val value = field.value
    when {
        field.id == "dr127" || field.id.contains("message") || field.id == "dr53" || field.id == "comment" -> {
            FieldValidationResult.from(value.length in 15..1000 && value.isNotBlank())
        }

        else -> FieldValidationResult.NOT_APPLICABLE
    }
}

fun List<FieldUi>.updateFieldAndResetError(field: FieldUi, newField: FieldUi): List<FieldUi> {
    val fieldIndex = indexOfFirst { field.id == it.id }
    return toMutableList().apply {
        set(fieldIndex, newField.resetError())
    }
}

fun FieldUi.resetError(): FieldUi {
    return copy(
        isError = false,
        supportingText = if (isError) "" else supportingText
    )

}

fun List<FieldUi>.updateField(field: FieldUi, newField: FieldUi): List<FieldUi> {
    val fieldIndex = indexOfFirst { field.id == it.id }
    return toMutableList().apply {
        set(fieldIndex, newField)
    }
}


fun FieldUi.getErrorText(getStringResource: (Int) -> String): String {
    return when {
        id == "email" || keyboardType == KeyboardType.Email -> {
            getStringResource(R.string.supporting_text_email)
        }

        id == "name" || id == "dr123" || id == "dr49" || id == "fio" -> {
            getStringResource(R.string.supporting_text_name)
        }

        id == "lastname" -> {
            getStringResource(R.string.supporting_text_lastname)
        }

        keyboardType == KeyboardType.Password -> {
            getStringResource(R.string.supporting_text_password)
        }

        id == "message" || id == "dr127" || id == "dr53" || id == "comment" -> {
            getStringResource(R.string.supporting_text_message)
        }

        id == "inn" -> {
            getStringResource(R.string.error_invalid_inn)
        }

        else -> ""
    }
}

inline fun List<FieldUi>.checkFields(
    putErrors: Boolean = false,
    validators: List<FieldValidator> = listOf(
        NoRequiredValidator,
        NameValidator,
        KeyboardTypeValidator
    ),
    getSupportingText: (FieldUi) -> String = { "" },
    onResult: (List<FieldUi>, isValid: Boolean) -> Unit = { p1, p2 -> },
): Boolean {
    var isValidFields = true

    val newFields = map { field ->
        val currentValidator = validators.firstOrNull { fieldValidator ->
            fieldValidator.isValid(field) != FieldValidationResult.NOT_APPLICABLE
        } ?: return@map field

        val isValid = currentValidator.isValid(field) == FieldValidationResult.VALID

        if (!isValid) {
            isValidFields = false
            if (putErrors) return@map field.copy(
                isError = true,
                supportingText = getSupportingText(field)
            )
        }
        field
    }

    onResult(newFields, isValidFields)

    return isValidFields
}

fun FieldUi.checkField(validators: List<FieldValidator> = vodovozValidators): Boolean {
    val currentValidator = validators.firstOrNull { fieldValidator ->
        fieldValidator.isValid(this) != FieldValidationResult.NOT_APPLICABLE
    } ?: return true

    return currentValidator.isValid(this) == FieldValidationResult.VALID
}

@JvmName("mapToFieldUiList")
fun List<FieldModel>.mapToUi(): List<FieldUi> {
    return map { it.toUi() }
}

fun FieldModel.toUi(): FieldUi {

    val contentType = when(id.lowercase()){
        "email" -> ContentType.Username
        "pass" -> ContentType.Password
        else -> null
    }
    val keyboardType = when (id.lowercase()) {
        "email", "emaildryg", "dr125", "dr51", "dr176" -> KeyboardType.Email
        "tel", "dr124", "phone", "dr50", "dr171", "dopphone" -> KeyboardType.Phone
        "pass", "parol" -> KeyboardType.Password
        "data", "date" -> KeyboardType.Unspecified
        "inn", FieldModel.BONUS_ID, FieldModel.CHANGE_ID -> KeyboardType.Number
        else -> when (valueType.lowercase()) {
            "text" -> KeyboardType.Text
            "phone" -> KeyboardType.Phone
            "email" -> KeyboardType.Email
            "number" -> KeyboardType.Number
            "password" -> KeyboardType.Password
            "date" -> KeyboardType.Decimal
            else -> KeyboardType.Unspecified
        }
    }
    val isDropDownField = valueType.uppercase() == "SPISOK"

    return FieldUi(
        id = id,
        label = label,
        value = values.firstOrNull { optionModel ->
            optionModel.id == value
        }?.value ?: value,
        keyboardType = if (isDropDownField) KeyboardType.Unspecified else keyboardType,
        isRequired = isRequired,
        isError = false,
        readOnly = readOnly,
        supportingText = supportingText,
        hint = hint,
        isValueVisible = keyboardType != KeyboardType.Password,
        type = if (isDropDownField) FieldTypeUi.DropDown(
            options = values.map { option ->
                DropDownOptionUi(option.id, option.value)
            }
        ) else FieldTypeUi.Text,
        isVisible = isVisible,
        contentType = contentType
    )
}

fun List<FieldUi>.mapToDomain(): List<FieldModel> {
    return map { it.toDomain() }
}

fun FieldUi.toDomain(): FieldModel {
    return FieldModel(
        id = id,
        value = value,
        valueType = when (keyboardType) {
            KeyboardType.Text -> "text"
            KeyboardType.Phone -> "phone"
            KeyboardType.Email -> "email"
            KeyboardType.Number -> "number"
            KeyboardType.Password -> "password"
            else -> "text"
        },
        isRequired = isRequired,
        readOnly = readOnly,
        supportingText = supportingText,
        label = label,
        hint = hint,
        values = if (type is FieldTypeUi.DropDown) {
            type.options.map { option ->
                FieldOptionModel(option.id, option.value)
            }
        } else {
            emptyList()
        }
    )
}