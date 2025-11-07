package com.m.vodovoz.feature.addresses.add.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.m.vodovoz.R
import com.m.vodovoz.design_system.composables.bottom_sheet.VodovozDragHandle
import com.m.vodovoz.design_system.composables.button.VodovozButtonsColumn
import com.m.vodovoz.design_system.composables.text_fields.VodovozTextField
import com.m.vodovoz.design_system.model.AddAddressLabelBSUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLabelBottomSheet(
    modifier: Modifier = Modifier,
    addAddressLabelBSUi: AddAddressLabelBSUi,
    state: SheetState = rememberModalBottomSheetState(true),
    onAddClick: (String) -> Unit,
    onValueChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        modifier = modifier.systemBarsPadding(),
        sheetState = state,
        onDismissRequest = onDismissRequest,
        dragHandle = {
            VodovozDragHandle()
        },
        containerColor = MaterialTheme.colorScheme.background,
        shape = MaterialTheme.shapes.large.copy(
            bottomStart = CornerSize(0.dp),
            bottomEnd = CornerSize(0.dp)
        ),
        contentWindowInsets = {
            WindowInsets(0, 0, 0, 0)
        }
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                modifier = Modifier.padding(top = 20.dp),
                text = addAddressLabelBSUi.title, color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall
            )

            VodovozTextField(
                modifier = Modifier.padding(
                    top = 8.dp
                ),
                value = addAddressLabelBSUi.value,
                onValueChange = onValueChange,
                hint = addAddressLabelBSUi.hint,
                trailingIcon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(
                            id = R.drawable.ic_close_circle
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .clickable { onValueChange("") },
                        tint = MaterialTheme.colorScheme.surfaceVariant
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions { onAddClick(addAddressLabelBSUi.value) }
            )

            VodovozButtonsColumn(
                modifier = Modifier.padding(vertical = 16.dp),
                buttons = listOf(addAddressLabelBSUi.button),
                onButtonClick = { onAddClick(addAddressLabelBSUi.value) }
            )
        }
    }
}