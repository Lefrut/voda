package com.m.vodovoz.feature.map.composables

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.m.vodovoz.design_system.modifiers.bottomLine
import com.m.vodovoz.feature.map.model.MapAddressUi

@Suppress("NonSkippableComposable")
@Composable
fun MapSearchList(
    modifier: Modifier = Modifier,
    recommendedAddresses: List<String>,
    onAddressClick: (String) -> Unit,
) {
    Column(
        modifier = modifier
            .animateContentSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 8.dp)
            .clip(
                shape = MaterialTheme.shapes.small.copy(
                    topStart = CornerSize(0.dp),
                    topEnd = CornerSize(0.dp)
                )
            )
            .verticalScroll(rememberScrollState())
            .imePadding()



    ) {
        recommendedAddresses.forEachIndexed { index, recommendedAddress ->
            Text(
                modifier = Modifier
                    .bottomLine(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        thickness = if (index == recommendedAddresses.lastIndex) 0.dp else 1.dp
                    )
                    .clickable { onAddressClick(recommendedAddress) }
                    .padding(16.dp)
                    .fillMaxWidth(),
                text = recommendedAddress,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}