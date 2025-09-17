package com.m.vodovoz.feature.cart.ordering.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.m.vodovoz.design_system.composables.button.VodovozOutlineButton
import com.m.vodovoz.feature.cart.ordering.model.OrderingMenuItemUi

@Suppress("NonSkippableComposable")
@Composable
fun OrderingPaymentColumn(
    modifier: Modifier = Modifier,
    title: String,
    items: List<OrderingMenuItemUi>,
    onPaymentButtonClick: (OrderingMenuItemUi) -> Unit,
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        if (title.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(bottom = 16.dp),
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { orderPaymentItem ->
                VodovozOutlineButton(
                    imagePainter = rememberAsyncImagePainter(model = orderPaymentItem.image),
                    name = orderPaymentItem.name,
                    description = orderPaymentItem.description,
                    error = orderPaymentItem.error,
                    onClick = { onPaymentButtonClick(orderPaymentItem) }
                )
            }
        }
    }
}