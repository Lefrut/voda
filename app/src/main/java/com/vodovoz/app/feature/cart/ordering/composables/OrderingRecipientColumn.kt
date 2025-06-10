package com.vodovoz.app.feature.cart.ordering.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.bottomLine
import com.vodovoz.app.feature.cart.ordering.model.OrderRecipientItemUi

@Suppress("NonSkippableComposable")
@Composable
fun OrderingRecipientColumn(
    modifier: Modifier = Modifier,
    title: String,
    items: List<OrderRecipientItemUi>,
    onItemClick: (OrderRecipientItemUi) -> Unit,
) {
    Column(modifier = modifier) {
        if (title.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        items.forEachIndexed { index, item ->
            val itemModifier = if (items.lastIndex == index) {
                Modifier
            } else {
                Modifier.bottomLine(MaterialTheme.colorScheme.surfaceVariant)
            }

            OrderRecipientItemButton(
                modifier = itemModifier,
                item = item,
                onClick = onItemClick
            )
        }
    }
}

@Composable
fun OrderRecipientItemButton(
    modifier: Modifier = Modifier,
    item: OrderRecipientItemUi,
    onClick: (OrderRecipientItemUi) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = { onClick(item) })
            .heightIn(56.dp)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.image,
            contentDescription = null,
            modifier = Modifier
                .padding(end = 16.dp)
                .size(24.dp),
            contentScale = ContentScale.FillBounds
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = item.description,
                color = MaterialTheme.colorScheme.surfaceTint,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.surfaceTint,
            modifier = Modifier
                .padding(start = 16.dp)
                .size(24.dp)
                .clickable { onClick(item) }
        )
    }
}