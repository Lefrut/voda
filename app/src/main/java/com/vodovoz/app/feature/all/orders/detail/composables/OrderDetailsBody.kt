package com.vodovoz.app.feature.all.orders.detail.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodovoz.app.design_system.composables.button.VodovozButtonsColumn
import com.vodovoz.app.design_system.composables.chip.OrderStatusChip
import com.vodovoz.app.design_system.composables.decoration.OrderSummaryColumn
import com.vodovoz.app.design_system.composables.decoration.VodovozHorizontalDivider
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.OrderProductUi
import com.vodovoz.app.design_system.model.order.OrderSummaryItemUi
import com.vodovoz.app.feature.all.orders.detail.model.OrderDetailsButtonUi
import com.vodovoz.app.feature.all.orders.detail.model.OrderDetailsSummaryUi
import com.vodovoz.app.feature.all.orders.detail.model.OrderStatusUi

@Suppress("NonSkippableComposable")
@Composable
fun OrderDetailsBody(
    modifier: Modifier = Modifier,
    header: String,
    currentStatuses: List<OrderStatusUi>,
    statuses: List<OrderStatusUi>,
    topButtons: List<OrderDetailsButtonUi>,
    productsTitle: String,
    products: List<OrderProductUi>,
    bottomButtons: List<ColorfulButtonUi>,
    orderSummary: List<OrderSummaryItemUi>,
    onTopButtonClick: (OrderDetailsButtonUi) -> Unit,
    onBottomButtonClick: (ColorfulButtonUi) -> Unit,
    onProductClick: (OrderProductUi) -> Unit,
    onProductLike: (OrderProductUi) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        if (header.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp),
                text = header,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 22.sp
                )
            )
        }

        if (currentStatuses.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                currentStatuses.forEach { status ->
                    OrderStatusChip(status = status)
                }
            }
        }

        val currentStepIndex = statuses.indexOfFirst { status ->
            currentStatuses.any { it.name == status.name }
        }.takeIf { it >= 0 }

        if (statuses.isNotEmpty() && currentStepIndex != null) {
            OrderProgressBar(
                modifier = Modifier.padding(
                    top = 35.dp,
                    bottom = 16.dp,
                    start = 48.dp,
                    end = 48.dp
                ),
                statuses = statuses.map { status -> status.name },
                currentStep = currentStepIndex
            )
        }

        OrderDetailsButtonsColumn(
            modifier = Modifier.padding(top = 24.dp),
            topButtons = topButtons,
            onButtonClick = onTopButtonClick
        )

        VodovozHorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (products.isNotEmpty()) {
            OrderDetailsProductColumn(
                title = productsTitle,
                products = products,
                onProductClick = onProductClick,
                onProductLike = onProductLike
            )
        }

        VodovozHorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        OrderSummaryColumn(
            modifier = Modifier.padding(top = 24.dp, start = 16.dp, end = 16.dp),
            items = orderSummary
        )

        VodovozButtonsColumn(
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp),
            buttons = bottomButtons,
            onButtonClick = onBottomButtonClick
        )
    }
}