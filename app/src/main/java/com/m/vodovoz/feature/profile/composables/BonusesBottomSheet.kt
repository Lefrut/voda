package com.m.vodovoz.feature.profile.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.m.vodovoz.R
import com.m.vodovoz.design_system.composables.bottom_sheet.VodovozDragHandle
import com.m.vodovoz.design_system.composables.button.VodovozButtonsColumn
import com.m.vodovoz.design_system.composables.swich.vodovozColors
import com.m.vodovoz.feature.profile.model.BonusesPopupWindowUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BonusesBottomSheet(
    modifier: Modifier = Modifier,
    data: BonusesPopupWindowUi,
    onDismissRequest: () -> Unit,
    onCopyClick: (String) -> Unit,
    onSubscribeChange: (Boolean) -> Unit,
    onConditionButtonClick: (BonusesPopupWindowUi) -> Unit,
) {
    ModalBottomSheet(
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(true),
        onDismissRequest = onDismissRequest,
        dragHandle = {
            VodovozDragHandle()
        },
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
    ) {
        Text(
            modifier = Modifier.padding(top = 10.dp, start = 16.dp, end = 16.dp),
            text = data.title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall
        )
        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .clickable { onCopyClick(data.coupon) }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    modifier = Modifier,
                    text = data.coupon,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = data.description,
                    color = MaterialTheme.colorScheme.surfaceTint,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_copy),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Row(
            modifier = Modifier
                .clickable {
                    onSubscribeChange(!data.warmAboutExpiration)
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = data.messageAboutExpiration,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium
            )
            Switch(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .requiredHeight(32.dp),
                checked = data.warmAboutExpiration,
                onCheckedChange = { onSubscribeChange(it) },
                colors = SwitchDefaults.vodovozColors()
            )
        }

        VodovozButtonsColumn(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 24.dp),
            buttons = listOf(data.button),
            onButtonClick = {
                onConditionButtonClick(data)
            }
        )

    }

}