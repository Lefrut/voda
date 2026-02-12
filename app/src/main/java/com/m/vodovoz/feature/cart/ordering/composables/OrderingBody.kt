package com.m.vodovoz.feature.cart.ordering.composables

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.m.vodovoz.design_system.composables.decoration.OrderSummaryColumn
import com.m.vodovoz.design_system.composables.decoration.VodovozHorizontalDivider
import com.m.vodovoz.design_system.model.SectionUi
import com.m.vodovoz.design_system.model.order.OrderSummaryItemUi
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.feature.cart.ordering.model.OrderNotifyItemUi
import com.m.vodovoz.feature.cart.ordering.model.OrderNotifySectionUi
import com.m.vodovoz.feature.cart.ordering.model.OrderingMenuItemUi

@Suppress("NonSkippableComposable")
@Composable
fun OrderingBody(
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
    paymentSection: SectionUi<OrderingMenuItemUi>,
    notifySection: OrderNotifySectionUi,
    selectedNotifyItem: OrderNotifyItemUi?,
    recipientSection: SectionUi<OrderingMenuItemUi>,
    totals: List<OrderSummaryItemUi>,
    onRecipientItemClick: (OrderingMenuItemUi) -> Unit,
    onNotifyItemSelect: (OrderNotifyItemUi) -> Unit,
    onPhoneFieldChange: (FieldUi, FieldUi) -> Unit,
    onPaymentButtonClick: (OrderingMenuItemUi) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        if (recipientSection.items.isNotEmpty()) {
            OrderingRecipientColumn(
                modifier = Modifier.padding(top = 16.dp),
                title = recipientSection.title,
                items = recipientSection.items,
                onItemClick = onRecipientItemClick
            )
        }

        VodovozHorizontalDivider()


        if (recipientSection.items.isNotEmpty() && notifySection.options.isNotEmpty()) {
            OrderingNotifyChips(
                modifier = Modifier.padding(top = 8.dp),
                notifySection = notifySection,
                selectedNotifyOption = selectedNotifyItem,
                onPhoneFieldChange = onPhoneFieldChange,
                onNotifyOptionSelect = onNotifyItemSelect,
            )
        }

        VodovozHorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (paymentSection.items.isNotEmpty()) {
            OrderingPaymentColumn(
                modifier = Modifier.padding(bottom = 24.dp),
                title = paymentSection.title,
                items = paymentSection.items,
                onPaymentButtonClick = onPaymentButtonClick
            )
        }

        OrderSummaryColumn(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 24.dp
            ),
            items = totals
        )
    }
}