package com.m.vodovoz.feature.cart.ordering.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import com.m.vodovoz.design_system.composables.text_fields.VodovozTextField
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.feature.cart.ordering.model.OrderNotifyItemUi
import com.m.vodovoz.feature.cart.ordering.model.OrderNotifySectionUi
import com.m.vodovoz.util.extensions.indexOfOrNull

@Composable
fun OrderingNotifyChips(
    modifier: Modifier = Modifier,
    notifySection: OrderNotifySectionUi,
    selectedNotifyOption: OrderNotifyItemUi?,
    onNotifyOptionSelect: (OrderNotifyItemUi) -> Unit,
    onPhoneFieldChange: (FieldUi, FieldUi) -> Unit,
) = with(notifySection) {
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
            selectedTabIndex = options.indexOfOrNull(selectedNotifyOption) ?: 0,
            edgePadding = 16.dp,
            spacing = 8.dp
        ) {
            options.forEach { option ->
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

        AnimatedVisibility(
            visible = selectedNotifyOption != options.firstOrNull() && selectedNotifyOption != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            extraPhoneField?.let {
                VodovozTextField(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    field = extraPhoneField,
                    onFieldChange = onPhoneFieldChange,
                )
            }
        }
    }
}