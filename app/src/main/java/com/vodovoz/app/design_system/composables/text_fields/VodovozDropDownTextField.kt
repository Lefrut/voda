package com.vodovoz.app.design_system.composables.text_fields

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodovoz.app.R
import com.vodovoz.app.design_system.model.widgets.FieldTypeUi
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.modifiers.bottomLine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VodovozDropDownTextField(
    modifier: Modifier = Modifier,
    field: FieldUi,
    minLines: Int = 1,
    maxLines: Int = minLines,
    onFieldChange: (FieldUi, FieldUi) -> Unit,
) {
    if (field.type !is FieldTypeUi.DropDown) return

    var showDropDown by rememberSaveable {
        mutableStateOf(false)
    }
    val animatedArrowRotation by animateFloatAsState(
        targetValue = if (showDropDown) 180f else 0f,
        label = "arrowRotation",
        animationSpec = tween(140, 0, LinearEasing)
    )
    ExposedDropdownMenuBox(
        modifier = Modifier.fillMaxWidth(),
        expanded = showDropDown,
        onExpandedChange = {
            if(!field.readOnly){
                showDropDown = !showDropDown
            }
        }
    ) {
        VodovozTextField(
            modifier = modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            value = field.value,
            onValueChange = { newValue ->
                onFieldChange(field, field.copy(value = newValue))
            },
            isError = field.isError,
            readOnly = true,
            label = field.label,
            hint = field.hint,
            maxLines = maxLines,
            minLines = minLines,
            supportingText = field.supportingText,
            visualTransformation = VisualTransformation.None,
            trailingIcon = {
                if(!field.readOnly){
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_down),
                        contentDescription = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { }
                            .size(24.dp)
                            .graphicsLayer { rotationZ = animatedArrowRotation },
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            prefix = null
        )


        ExposedDropdownMenu(
            expanded = showDropDown,
            onDismissRequest = { showDropDown = false },
            shape = MaterialTheme.shapes.small,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            val options = field.type.options
            options.forEachIndexed { index, option ->
                val textModifier = if (options.lastIndex == index) Modifier
                else Modifier.bottomLine(MaterialTheme.colorScheme.surfaceTint)

                Text(
                    modifier = textModifier
                        .fillMaxWidth()
                        .clickable(onClick = {
                            onFieldChange(
                                field,
                                field.copy(
                                    value = option.value
                                )
                            )
                            showDropDown = false
                        }
                        )
                        .padding(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        ),
                    text = option.value,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        letterSpacing = 0.sp
                    )
                )
            }
        }

    }

}