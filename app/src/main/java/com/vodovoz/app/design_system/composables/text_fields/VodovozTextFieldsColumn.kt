package com.vodovoz.app.design_system.composables.text_fields

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.vodovoz.app.design_system.model.widgets.FieldUi

@Suppress("NonSkippableComposable")
@Composable
fun VodovozTextFieldsColumn(
    modifier: Modifier = Modifier,
    fields: List<FieldUi>,
    onFieldChange: (currentField: FieldUi, newField: FieldUi) -> Unit,
    onFieldClick: (FieldUi) -> Unit = {},
    onDone: KeyboardActionScope.() -> Unit = {}
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        fields.forEachIndexed { index, field ->
            key(field.id) {
                VodovozTextField(
                    modifier = Modifier.pointerInput(Unit){
                        awaitEachGesture {
                            awaitFirstDown(pass = PointerEventPass.Initial)
                            val up = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                            if(up != null && !field.readOnly) {
                                onFieldClick(field)
                            }
                        }
                    },
                    field = field,
                    onFieldChange = onFieldChange,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = field.keyboardType,
                        imeAction = if (fields.lastIndex == index) ImeAction.Done else ImeAction.Next,
                    ),
                    onDone = onDone
                )
            }
        }
    }
}