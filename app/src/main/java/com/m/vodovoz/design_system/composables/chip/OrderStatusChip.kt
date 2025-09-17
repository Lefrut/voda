package com.m.vodovoz.design_system.composables.chip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.feature.all.orders.detail.model.OrderStatusUi

@Composable
fun OrderStatusChip(modifier: Modifier = Modifier, status: OrderStatusUi) {
    Row(
        modifier = modifier
            .background(
                color = status.background.copy(status.backgroundAlpha),
                shape = MaterialTheme.shapes.medium
            )
            .padding(end = 8.dp, start = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            painter = rememberAsyncImagePainter(
                model = status.icon,
                contentScale = ContentScale.FillBounds
            ),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = status.color
        )
        Text(
            text = status.name,
            color = status.color,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Preview(apiLevel = 34)
@Composable
private fun OrderStatusChipPreview() {
    VodovozTheme {
        OrderStatusChip(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            status = OrderStatusUi(
                "Не оплачен",
                "",
                Color.Red,
                Color(0xFFF91155).copy(0.05f),
                0.05f
            )
        )
    }
}