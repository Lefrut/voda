package com.m.vodovoz.feature.auth.login_by_phone_code.composables

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



@Composable
fun OtpTextField(
    modifier: Modifier = Modifier,
    otpCount: Int,
    otpText: String,
    onOtpTextChange: (String) -> Unit,
    onDone: () -> Unit,
) {
    BasicTextField(
        modifier = modifier,
        value = TextFieldValue(otpText, selection = TextRange(otpText.length)),
        onValueChange = onValueChange@{ fieldValue ->
            if (fieldValue.text.length > otpCount) return@onValueChange

            onOtpTextChange(fieldValue.text)

        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                onDone()
            }
        ),
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    16.dp,
                    Alignment.CenterHorizontally
                ),
            ) {
                repeat(otpCount) { index ->
                    OtpCell(
                        value = otpText.getOrNull(index)?.toString() ?: "",
                        selected = index <= otpText.length - 1
                    )
                }
            }
        }
    )
}

@Composable
private fun OtpCell(
    modifier: Modifier = Modifier,
    value: String,
    selected: Boolean,
) {
    Text(
        modifier = modifier
            .width(42.dp)
            .height(48.dp)
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .wrapContentSize(Alignment.Center),
        text = value,
        style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.sp),
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center
    )
}