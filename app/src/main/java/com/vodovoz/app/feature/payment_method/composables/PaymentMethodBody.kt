package com.vodovoz.app.feature.payment_method.composables

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.vodovoz.app.design_system.composables.button.VodovozRadioButton
import com.vodovoz.app.design_system.composables.swich.vodovozColors
import com.vodovoz.app.design_system.composables.text_fields.VodovozTextField
import com.vodovoz.app.design_system.model.SectionUi
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.WidgetUi
import com.vodovoz.app.feature.payment_method.model.PaymentMethodItemUi

@Suppress("NonSkippableComposable")
@Composable
fun PaymentMethodBody(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    paymentSections: List<SectionUi<PaymentMethodItemUi>>,
    onPaymentItemClick: (PaymentMethodItemUi) -> Unit,
    onFieldChange: (PaymentMethodItemUi, FieldUi, FieldUi) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
    ) {
        paymentSections.forEachIndexed { index, section ->
            item(key = section.title + index, contentType = "SectionTitle") {
                Text(
                    text = section.title,
                    color = MaterialTheme.colorScheme.surfaceTint,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            itemsIndexed(
                items = section.items,
                key = { _, item -> (item.id + item.code + item.name) },
                contentType = { _, _ -> "PaymentMethodItemUi" }
            ) { i, item ->
                PaymentMethodItemRow(
                    item = item,
                    onItemClick = onPaymentItemClick,
                    onFieldChange = { field, updatedField ->
                        onFieldChange(item, field, updatedField)
                    }
                )

                if (i != section.items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(horizontal = 8.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        item(key = "LastHorizontalDivider") {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
        }


    }
}

@Composable
private fun PaymentMethodItemRow(
    modifier: Modifier = Modifier,
    item: PaymentMethodItemUi,
    onItemClick: (PaymentMethodItemUi) -> Unit,
    onFieldChange: (FieldUi, FieldUi) -> Unit,
) {
    Column(
        Modifier.background(
            MaterialTheme.colorScheme.background
        ).animateContentSize()
    ) {
        Row(
            modifier = modifier
                .clickable { onItemClick(item) }
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.image,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )

            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                text = item.name,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium
            )

            when (item.isSwitch) {
                true -> {
                    Switch(
                        modifier = Modifier.requiredHeight(32.dp),
                        checked = item.value,
                        onCheckedChange = {
                            onItemClick(item)
                        },
                        colors = SwitchDefaults.vodovozColors()
                    )
                }

                false -> {
                    VodovozRadioButton(
                        selected = item.value,
                        onClick = { onItemClick(item) }
                    )
                }
            }
        }

        if (item.field != null && item.value) {
            VodovozTextField(
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
                field = item.field,
                onFieldChange = onFieldChange
            )
        }
    }
}