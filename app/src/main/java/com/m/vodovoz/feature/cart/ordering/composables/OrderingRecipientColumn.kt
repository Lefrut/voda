package com.m.vodovoz.feature.cart.ordering.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.m.vodovoz.R
import com.m.vodovoz.design_system.composables.swich.vodovozColors
import com.m.vodovoz.design_system.modifiers.bottomLine
import com.m.vodovoz.feature.cart.ordering.model.OrderingMenuItemType
import com.m.vodovoz.feature.cart.ordering.model.OrderingMenuItemUi

@Composable
fun OrderingRecipientColumn(
    modifier: Modifier = Modifier,
    title: String,
    items: List<OrderingMenuItemUi>,
    onItemClick: (OrderingMenuItemUi) -> Unit,
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


            OrderRecipientItem(
                modifier = itemModifier,
                item = item,
                onClick = onItemClick
            )
        }


    }
}


@Composable
private fun OrderRecipientItem(
    modifier: Modifier = Modifier,
    item: OrderingMenuItemUi,
    onClick: (OrderingMenuItemUi) -> Unit,
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
            if (item.description.isNotEmpty()) {
                Text(
                    text = item.description,
                    color = if (item.error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceTint,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }


        when (item.type) {
            OrderingMenuItemType.Default -> {
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

            OrderingMenuItemType.Switch -> {
                Switch(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .requiredHeight(32.dp),
                    checked = item.value.toBoolean(),
                    onCheckedChange = {
                        onClick(item.copy(value = it.toString()))
                    },
                    colors = SwitchDefaults.vodovozColors(),
                )
            }
        }
    }
}

