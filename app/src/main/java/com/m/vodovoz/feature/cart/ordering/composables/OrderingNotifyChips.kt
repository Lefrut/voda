package com.m.vodovoz.feature.cart.ordering.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.m.vodovoz.design_system.composables.chip.VodovozChip
import com.m.vodovoz.design_system.composables.tab_row.VodovozScrollableTabRow
import com.m.vodovoz.feature.cart.ordering.model.OrderNotifyItemUi
import com.m.vodovoz.util.extensions.indexOfOrNull

@Suppress("NonSkippableComposable")
@Composable
fun OrderingNotifyChips(
    modifier: Modifier = Modifier,
    title: String,
    notifyOptions: List<OrderNotifyItemUi>,
    selectedNotifyOption: OrderNotifyItemUi,
    onNotifyOptionSelect: (OrderNotifyItemUi) -> Unit,
) {
    Column(modifier = modifier) {
        if (title.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall
            )
        }

        VodovozScrollableTabRow(
            selectedTabIndex = notifyOptions.indexOfOrNull(selectedNotifyOption) ?: 0,
            edgePadding = 16.dp,
            spacing = 8.dp
        ) {
            notifyOptions.forEach { option ->
                val selected = selectedNotifyOption == option
                key(option.value) {
                    VodovozChip(
                        text = option.value,
                        selected = selected,
                        onSelect = { onNotifyOptionSelect(option) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp),
                        containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        contentColor = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground,
                        shape = RoundedCornerShape(20.dp),
                        borderStroke = null
                    )
                }
            }
        }
    }
}