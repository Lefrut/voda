package com.m.vodovoz.design_system.composables.bottom_sheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m.vodovoz.R
import com.m.vodovoz.design_system.composables.button.VodovozButton
import com.m.vodovoz.design_system.composables.button.VodovozButtonDefaults
import com.m.vodovoz.design_system.composables.button.VodovozButtonsColumn
import com.m.vodovoz.design_system.composables.text_fields.VodovozTextField
import com.m.vodovoz.design_system.model.widgets.FieldPopupWindowUi
import com.m.vodovoz.design_system.model.widgets.FieldUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldBottomSheet(
    modifier: Modifier = Modifier,
    data: FieldPopupWindowUi,
    onDismissRequest: () -> Unit,
    onButtonClick: () -> Unit,
    onFieldChange: (FieldUi, FieldUi) -> Unit
) {
    val title = data.title
    val description = data.description

    ModalBottomSheet(
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(true),
        onDismissRequest = onDismissRequest,
        dragHandle = {
            VodovozDragHandle()
        },
        containerColor = MaterialTheme.colorScheme.background,
        shape = MaterialTheme.shapes.large.copy(
            bottomStart = CornerSize(0.dp),
            bottomEnd = CornerSize(0.dp)
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            if (title.isNotEmpty()) {
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = title,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            if (description.isNotEmpty()) {
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = description,
                    color = MaterialTheme.colorScheme.surfaceTint,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        letterSpacing = 0.sp
                    )
                )
            }

            VodovozTextField(
                modifier = Modifier.padding(top = 8.dp),
                field = data.field,
                onFieldChange = onFieldChange
            )



            VodovozButtonsColumn(
                buttons = listOf(data.button),
                modifier = Modifier.padding(top = 20.dp),
                onButtonClick = {
                    onButtonClick()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

}