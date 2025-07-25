package com.vodovoz.app.feature.order_call_you.copmosables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.vodovoz.app.feature.order_call_you.model.CallYouItemUi

@Suppress("NonSkippableComposable")
@Composable
fun OrderCallYouBody(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    items: List<CallYouItemUi>,
    currentItem: CallYouItemUi,
    onItemSelect: (CallYouItemUi) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
    ) {
        items.forEach { item ->
            key(item.value) {
                Column {
                    CallYouItemRadioButton(
                        item = item,
                        onItemSelect = onItemSelect,
                        selected = item == currentItem
                    )
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

            }
        }
    }
}

@Composable
private fun CallYouItemRadioButton(
    modifier: Modifier = Modifier,
    item: CallYouItemUi,
    selected: Boolean,
    onItemSelect: (CallYouItemUi) -> Unit,
) {
    Row(
        modifier = modifier
            .alpha(if (item.enabled) 1f else 0.4f)
            .clickable {
                if (item.enabled) {
                    onItemSelect(item)
                }
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        ) {
            Text(
                text = item.name,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium
            )
            if (item.description.isNotBlank()) {
                Text(
                    text = item.description,
                    color = MaterialTheme.colorScheme.surfaceTint,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        RadioButton(selected = selected, onClick = { onItemSelect(item) })
    }
}