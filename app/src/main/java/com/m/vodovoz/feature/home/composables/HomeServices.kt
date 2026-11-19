package com.m.vodovoz.feature.home.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.m.vodovoz.common.model.ButtonAction
import com.m.vodovoz.design_system.model.SectionUi
import com.m.vodovoz.feature.bottom.services.composables.ServiceCard
import com.m.vodovoz.feature.bottom.services.model.ServiceUi

@Composable
internal fun HomeServices(
    modifier: Modifier = Modifier,
    section: SectionUi<ServiceUi>,
    onShowAllClick: (ButtonAction) -> Unit,
    onServiceClick: (ServiceUi) -> Unit,
) {
    Column(modifier = modifier) {
        TitleAndButton(
            title = section.title,
            button = section.button,
            onShowAllClick = onShowAllClick,
        )

        AutoScrollPager(
            modifier = Modifier.padding(top = 16.dp),
            pagerState = rememberAutoScrollPagerState(itemsCount = section.items.size),
            itemsCount = section.items.size,
            pageWidth = 300.dp,
        ) { index ->
            ServiceCard(
                service = section.items[index],
                outlined = true,
                onClick = onServiceClick,
            )
        }
    }
}
