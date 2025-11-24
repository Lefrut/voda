package com.m.vodovoz.feature.addresses.add.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m.vodovoz.R
import com.m.vodovoz.design_system.model.AddressLabelChip
import com.m.vodovoz.design_system.model.AddressLabelUi

@Composable
fun AddAddressLabels(
    modifier: Modifier = Modifier,
    labels: List<AddressLabelUi>,
    currentLabel: AddressLabelUi,
    onRemove: (AddressLabelUi) -> Unit,
    onClick: (AddressLabelUi) -> Unit,
    onAdd: () -> Unit,
) {
    Column {
        Text(
            modifier = modifier.padding(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp
            ),
            text = stringResource(id = R.string.labels),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall
        )

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            labels.distinctBy { it.name }.filter { it.name.isNotBlank() }.forEach { label ->
                val uniqueName = label.name
                key(uniqueName) {
                    val selected = uniqueName == currentLabel.name
                    AddressLabelChip(
                        addressLabel = label,
                        selected = selected,
                        onClick = onClick,
                        onRemove = onRemove
                    )
                }
            }

            Row(
                modifier = Modifier
                    .height(30.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable(onClick = onAdd)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                Text(
                    text = stringResource(R.string.add),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 0.1.sp),
                    maxLines = 1
                )

                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_plus),
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onAdd),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

}