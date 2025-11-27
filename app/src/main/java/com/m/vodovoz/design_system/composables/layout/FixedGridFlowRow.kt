package com.m.vodovoz.design_system.composables.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.floor
import kotlin.math.min

@Composable
fun FixedGridFlowRow(
    modifier: Modifier = Modifier,
    itemsInRow: Int,
    horizontalSpacing: Dp,
    verticalSpacing: Dp,
    content: @Composable FlowRowScope.(itemWidth: Dp) -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val itemWidth = FixedGridFlowRowDefaults.calcItemWidth(
            totalWidth = this.maxWidth,
            spacing = horizontalSpacing,
            itemsInRow = itemsInRow
        )

        @Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
            maxItemsInEachRow = itemsInRow,
            maxLines = Int.MAX_VALUE
        ) {
            content(itemWidth)
        }
    }
}


data object FixedGridFlowRowDefaults {

    @Stable
    fun calcItemWidth(totalWidth: Dp, spacing: Dp, itemsInRow: Int): Dp {
        val totalSpacing = spacing * (itemsInRow - 1)
        val raw = (totalWidth - totalSpacing) / itemsInRow
        val w = floor(raw.value)
        return Dp(min(w, raw.value)) - 1.dp
    }

}

