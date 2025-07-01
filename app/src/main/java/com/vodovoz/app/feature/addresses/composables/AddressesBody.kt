package com.vodovoz.app.feature.addresses.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.bottomLine
import com.vodovoz.app.design_system.composables.button.VodovozRadioButton
import com.vodovoz.app.design_system.composables.decoration.VodovozHorizontalDivider
import com.vodovoz.app.design_system.composables.decoration.VodovozSwipeToDismiss
import com.vodovoz.app.design_system.model.SectionUi
import com.vodovoz.app.feature.addresses.model.AddressScreenTypeUi
import com.vodovoz.app.feature.addresses.model.AddressUi

@Suppress("NonSkippableComposable")
@Composable
fun AddressesBody(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    addressSections: List<SectionUi<AddressUi>>,
    screenTypeUi: AddressScreenTypeUi,
    selectedAddress: AddressUi,
    onAddressSelect: (AddressUi) -> Unit,
    onEditAddressClick: (AddressUi) -> Unit,
    onRemoveAddressSwipe: (AddressUi) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding
    ) {
        addressSections.forEachIndexed { index, addressSection ->
            if (screenTypeUi == AddressScreenTypeUi.Choose && addressSection.items.isNotEmpty()) {
                item {
                    Text(
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        ),
                        text = addressSection.title,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            itemsIndexed(
                items = addressSection.items,
                key = { _, address -> address.id }
            ) { i, address ->

                VodovozSwipeToDismiss(
                    modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null),
                    onRemove = { onRemoveAddressSwipe(address) }
                ) {
                    AddressItemCard(
                        modifier = if (i != addressSection.items.lastIndex) Modifier.bottomLine(
                            MaterialTheme.colorScheme.surfaceVariant
                        ) else Modifier,
                        address = address,
                        screenTypeUi = screenTypeUi,
                        selected = selectedAddress.id == address.id,
                        onClick = onAddressSelect,
                        onEditClick = onEditAddressClick
                    )

                }
            }

            if (index != addressSections.lastIndex && screenTypeUi != AddressScreenTypeUi.Add && addressSection.items.isNotEmpty()) {
                item {
                    VodovozHorizontalDivider()
                }
            } else if (addressSections.lastOrNull()?.items?.lastOrNull() != null) {
                item {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AddressItemCard(
    modifier: Modifier = Modifier,
    screenTypeUi: AddressScreenTypeUi,
    address: AddressUi,
    selected: Boolean,
    onClick: (AddressUi) -> Unit,
    onEditClick: (AddressUi) -> Unit,
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .heightIn(56.dp)
            .fillMaxWidth()
            .clickable(onClick = { onClick(address) })
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (screenTypeUi == AddressScreenTypeUi.Choose) {
            VodovozRadioButton(
                modifier = Modifier.padding(end = 16.dp),
                selected = selected,
                onClick = { onClick(address) }
            )
        }

        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (address.description.isNotEmpty() && screenTypeUi != AddressScreenTypeUi.Choose) {
                Text(
                    text = address.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.15.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = address.address.replaceFirstChar { char ->
                    char.uppercaseChar()
                },
                style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.sp),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_edit),
            contentDescription = null,
            modifier = Modifier
                .padding(start = 16.dp)
                .clip(MaterialTheme.shapes.small)
                .size(24.dp)
                .clickable { onEditClick(address) },
            tint = MaterialTheme.colorScheme.surfaceTint
        )
    }
}