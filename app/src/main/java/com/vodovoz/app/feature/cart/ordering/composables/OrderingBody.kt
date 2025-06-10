package com.vodovoz.app.feature.cart.ordering.composables

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vodovoz.app.design_system.composables.button.VodovozButton
import com.vodovoz.app.design_system.composables.decoration.OrderSummaryColumn
import com.vodovoz.app.design_system.composables.decoration.VodovozHorizontalDivider
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.SectionUi
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.order.OrderSummaryItemUi
import com.vodovoz.app.feature.cart.ordering.model.OrderNotifyItemUi
import com.vodovoz.app.feature.cart.ordering.model.OrderPaymentItemUi
import com.vodovoz.app.feature.cart.ordering.model.OrderRecipientItemUi

@Suppress("NonSkippableComposable")
@Composable
fun OrderingBody(
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
    comment: FieldUi?,
    paymentSection: SectionUi<OrderPaymentItemUi>,
    notifySection: SectionUi<OrderNotifyItemUi>,
    selectedNotifyItem: OrderNotifyItemUi,
    recipientSection: SectionUi<OrderRecipientItemUi>,
    totals: List<OrderSummaryItemUi>,
    button: ColorfulButtonUi,
    onRecipientItemClick: (OrderRecipientItemUi) -> Unit,
    onCommentChange: (FieldUi, FieldUi) -> Unit,
    onNotifyItemSelect: (OrderNotifyItemUi) -> Unit,
    onPaymentButtonClick: (OrderPaymentItemUi) -> Unit,
    onButtonClick: () -> Unit,
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

        if (comment != null) {
            OrderingCommentColumn(
                modifier = Modifier.padding(top = 8.dp),
                comment = comment,
                onFieldChange = onCommentChange
            )
        }


        if (recipientSection.items.isNotEmpty()) {
            OrderingNotifyChips(
                modifier = Modifier.padding(top = 24.dp),
                title = notifySection.title,
                notifyOptions = notifySection.items,
                selectedNotifyOption = selectedNotifyItem,
                onNotifyOptionSelect = onNotifyItemSelect
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
            modifier = Modifier.padding(horizontal = 16.dp),
            items = totals
        )

        VodovozButton(
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp),
            text = button.name,
            onClick = onButtonClick
        )

    }
}