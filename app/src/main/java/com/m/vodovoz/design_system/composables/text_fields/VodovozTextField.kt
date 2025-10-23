package com.m.vodovoz.design_system.composables.text_fields

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.m.vodovoz.R
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.decoration.PasswordIcon
import com.m.vodovoz.design_system.model.widgets.FieldTypeUi
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.design_system.modifiers.isElementVisible
import com.m.vodovoz.design_system.text.PhoneNumberVisualTransformation
import com.m.vodovoz.util.formatRussianPhoneNumber

@Composable
fun VodovozTextField(
    modifier: Modifier = Modifier,
    field: FieldUi,
    onFieldChange: (currentField: FieldUi, newField: FieldUi) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onDone: (KeyboardActionScope.() -> Unit)? = null,
    maxLines: Int = 1,
    minLines: Int = 1,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isMessage = field.id.contains(
        "message",
        true
    ) || field.id == "dr127" || field.id == "dr53" || field.id == "comment" || field.id == "description"

    val visualTransformation = when (field.keyboardType) {
        KeyboardType.Phone -> PhoneNumberVisualTransformation()
        KeyboardType.Password -> if (!field.isValueVisible) {
            PasswordVisualTransformation('•')
        } else VisualTransformation.None

        else -> VisualTransformation.None
    }

    if (visualTransformation is PhoneNumberVisualTransformation) {
        LaunchedEffect(isFocused) {
            if (isFocused) onFieldChange(field, field)
        }
    }

    if(field.isVisible){
        when (field.type) {
            is FieldTypeUi.DropDown -> {
                VodovozDropDownTextField(
                    modifier = modifier,
                    field = field,
                    onFieldChange = onFieldChange
                )
            }

            FieldTypeUi.Text -> {
                VodovozTextField(
                    modifier = modifier,
                    value = field.value,
                    onValueChange = { newValue ->
                        onFieldChange(field, field.copy(value = newValue))
                    },
                    isError = field.isError,
                    keyboardOptions = keyboardOptions.copy(
                        keyboardType = field.keyboardType
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (onDone != null) { onDone() }
                            else { keyboardController?.hide() }
                        }
                    ),
                    readOnly = field.readOnly,
                    label = field.label,
                    hint = field.hint,
                    maxLines = if (isMessage) 3 else maxLines,
                    minLines = if (isMessage) 2 else minLines,
                    supportingText = field.supportingText,
                    visualTransformation = visualTransformation,
                    trailingIcon = {
                        if (field.keyboardType == KeyboardType.Password) {
                            PasswordIcon(valueIsVisible = field.isValueVisible) {
                                onFieldChange(
                                    field,
                                    field.copy(
                                        isValueVisible = !field.isValueVisible
                                    )
                                )
                            }
                        }
                    },
                    prefix = null,
                    interactionSource = interactionSource
                )
            }
        }
    }
}

@Composable
fun BaseVodovozTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground),
    hint: String = "",
    label: String? = null,
    supportingText: String? = null,
    prefix: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: VodovozTextFieldColors = VodovozTextFieldDefaults.colors(),
) {
    val isFocused by interactionSource.collectIsFocusedAsState()

    BasicTextField(
        value = value,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        onValueChange = { newTextFieldValue ->
            onValueChange(newTextFieldValue)
        },
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        singleLine = maxLines == 1 || singleLine,
        maxLines = maxLines,
        minLines = minLines,
    ) { innerTextField ->
        Column {
            if (!label.isNullOrEmpty()) {
                Text(
                    text = label,
                    color = if (isFocused) colors.focusedLabelColor else colors.labelColor,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(48.dp)
                    .border(
                        width = 1.dp,
                        color = if (isError) colors.errorColor else if (isFocused) colors.focusedBorderColor else colors.borderColor,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                prefix?.let {
                    Text(
                        modifier = Modifier.padding(end = 6.dp),
                        text = prefix,
                        color = MaterialTheme.colorScheme.surfaceTint,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = maxLines
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopStart
                ) {
                    if (value.text.isEmpty()) {
                        Text(
                            modifier = Modifier
                                .matchParentSize()
                                .horizontalScroll(rememberScrollState()),
                            text = hint,
                            color = MaterialTheme.colorScheme.surfaceTint,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = maxLines,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    innerTextField()
                }
                trailingIcon?.let { trailingIcon() }
            }

            if (!supportingText.isNullOrEmpty()) {
                Text(
                    text = supportingText,
                    color = if (!isError) {
                        colors.supportingTextColor
                    } else colors.errorColor,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}


@Composable
fun VodovozTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onBackground
    ),
    hint: String = "",
    label: String? = null,
    supportingText: String? = null,
    prefix: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    isVisible: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    minLines: Int = 1,
    maxLines: Int = minLines,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: VodovozTextFieldColors = VodovozTextFieldDefaults.colors(),
) {
    var textFieldValueState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = value))
    }
    val textFieldValue = textFieldValueState.copy(text = value)
    val haveFocus by interactionSource.collectIsFocusedAsState()
    val russianPhoneCode = stringResource(id = R.string.russian_phone_code)
    val isPhone = visualTransformation is PhoneNumberVisualTransformation

    LaunchedEffect(haveFocus) {
        if (haveFocus && !textFieldValue.text.startsWith(russianPhoneCode) && isPhone) {
            val newText = formatRussianPhoneNumber(value)
            textFieldValueState = textFieldValueState.copy(
                text = newText,
                selection = TextRange(russianPhoneCode.length)
            )
            onValueChange(newText)
        }
    }

    SideEffect {
        if (
            textFieldValue.selection != textFieldValueState.selection ||
            textFieldValue.composition != textFieldValueState.composition
        ) {
            textFieldValueState = textFieldValue
        }
    }

    var lastTextValue by remember(value) { mutableStateOf(value) }

    if(isVisible){
        BaseVodovozTextField(
            modifier = modifier,
            value = textFieldValue,
            onValueChange = onValueChange@{ newTextFieldValueState ->
                if (readOnly) return@onValueChange

                val newText =
                    if (isPhone) formatRussianPhoneNumber(newTextFieldValueState.text) else newTextFieldValueState.text
                val newSelection =
                    if (newTextFieldValueState.selection.start < russianPhoneCode.length && isPhone) {
                        TextRange(russianPhoneCode.length)
                    } else newTextFieldValueState.selection

                textFieldValueState = newTextFieldValueState.copy(
                    text = newText,
                    selection = newSelection
                )

                val stringChangedSinceLastInvocation = lastTextValue != newText
                lastTextValue = newText

                if (stringChangedSinceLastInvocation) {
                    onValueChange(newText)
                }
            },
            supportingText = supportingText,
            label = label,
            textStyle = textStyle,
            hint = hint,
            prefix = prefix,
            enabled = enabled,
            readOnly = readOnly,
            keyboardActions = keyboardActions,
            keyboardOptions = keyboardOptions,
            isError = isError,
            interactionSource = interactionSource,
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            maxLines = maxLines,
            minLines = minLines,
            colors = colors
        )
    }
}


@Immutable
data class VodovozTextFieldColors(
    val focusedLabelColor: Color,
    val labelColor: Color,
    val supportingTextColor: Color,
    val textColor: Color,
    val focusedTextColor: Color,
    val borderColor: Color,
    val focusedBorderColor: Color,
    val errorColor: Color,
)

@Stable
data object VodovozTextFieldDefaults {

    @Composable
    fun colors(
        focusedLabelColor: Color = MaterialTheme.colorScheme.surfaceTint,
        labelColor: Color = MaterialTheme.colorScheme.surfaceTint,
        supportingTextColor: Color = MaterialTheme.colorScheme.surfaceTint,
        textColor: Color = MaterialTheme.colorScheme.onBackground,
        focusedTextColor: Color = MaterialTheme.colorScheme.onBackground,
        borderColor: Color = MaterialTheme.colorScheme.surfaceVariant,
        focusedBorderColor: Color = MaterialTheme.colorScheme.primary,
        errorColor: Color = MaterialTheme.colorScheme.error,
    ) = VodovozTextFieldColors(
        focusedLabelColor = focusedLabelColor,
        labelColor = labelColor,
        supportingTextColor = supportingTextColor,
        textColor = textColor,
        focusedTextColor = focusedTextColor,
        borderColor = borderColor,
        focusedBorderColor = focusedBorderColor,
        errorColor = errorColor
    )

}

@Preview(apiLevel = 34)
@Composable
private fun VodovozTextFieldPreview() {
    VodovozTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(300.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {

            val value = remember {
                mutableStateOf("")
            }

            VodovozTextField(
                modifier = Modifier.padding(horizontal = 16.dp),
                value = value.value,
                hint = "Введите значение",
                onValueChange = {
                    value.value = it
                },
                label = "Поле ввода",
                supportingText = "",
                isError = false,
                prefix = "от",
                minLines = 1,
                maxLines = 1
            )
        }
    }
}
