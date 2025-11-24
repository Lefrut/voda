package com.m.vodovoz.design_system.model

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m.vodovoz.R
import com.m.vodovoz.domain.general.model.location.AddressLabelModel

@Immutable
data class AddressLabelUi(
    val id: String,
    val name: String,
    val isRemoveable: Boolean,
) {
    companion object {
        val Empty = AddressLabelUi("", "", false)
    }
}

fun AddressLabelModel.toUi(): AddressLabelUi {
    return AddressLabelUi(
        id, name, isRemoveable
    )
}

fun List<AddressLabelModel>.mapToUi(): List<AddressLabelUi> {
    return map { it.toUi() }
}

@Composable
fun AddressLabelChip(
    modifier: Modifier = Modifier,
    addressLabel: AddressLabelUi,
    selected: Boolean,
    onClick: (AddressLabelUi) -> Unit,
    onRemove: (AddressLabelUi) -> Unit,
) {
    Row(
        modifier = modifier
            .height(30.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
            .padding(horizontal = 12.dp)
            .clickable(onClick = { onClick(addressLabel) }),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(
            space = 9.5.dp,
            alignment = Alignment.CenterHorizontally
        )
    ) {
        Text(
            text = addressLabel.name,
            color = if (selected) {
                MaterialTheme.colorScheme.background
            } else {
                MaterialTheme.colorScheme.onBackground
            },
            style = MaterialTheme.typography.bodySmall.copy(
                letterSpacing = 0.1.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (addressLabel.isRemoveable) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_close_circle),
                contentDescription = null,
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .clickable(onClick = { onRemove(addressLabel) }),
                tint = if (selected) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    MaterialTheme.colorScheme.surfaceTint
                }
            )
        }

    }

}
