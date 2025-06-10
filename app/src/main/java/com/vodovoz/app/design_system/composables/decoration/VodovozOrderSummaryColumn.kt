package com.vodovoz.app.design_system.composables.decoration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vodovoz.app.design_system.model.order.OrderSummaryItemUi

@Suppress("NonSkippableComposable")
@Composable
fun OrderSummaryColumn(modifier: Modifier = Modifier, items: List<OrderSummaryItemUi>) {
    Column(modifier = modifier) {
        items.forEachIndexed { index, item ->
            val isFirstItem = index == 0
            val updatedItem = item.copy(
                color = if (isFirstItem) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    item.color
                }
            )

            OrderSummaryItem(
                item = updatedItem,
                style = if (isFirstItem) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.bodySmall
                },
                nameColor = if (isFirstItem) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    MaterialTheme.colorScheme.surfaceTint
                }
            )

            Spacer(
                modifier = Modifier.height(
                    when (index) {
                        0 -> 16.dp
                        items.lastIndex -> 0.dp
                        else -> 4.dp
                    }
                )
            )
        }
    }
}

@Composable
private fun OrderSummaryItem(
    modifier: Modifier = Modifier,
    item: OrderSummaryItemUi,
    nameColor: Color = MaterialTheme.colorScheme.surfaceTint,
    style: TextStyle = MaterialTheme.typography.bodySmall,
) {
    Row(modifier = modifier) {
        Text(
            modifier = Modifier.weight(1f),
            text = item.name,
            color = nameColor,
            style = style
        )
        Text(
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f),
            text = item.value,
            color = item.color.takeOrElse {
                MaterialTheme.colorScheme.surfaceTint
            },
            style = style,
            textAlign = TextAlign.End
        )

    }
}