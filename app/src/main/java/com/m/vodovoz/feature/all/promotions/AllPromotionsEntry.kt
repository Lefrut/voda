package com.m.vodovoz.feature.all.promotions

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToPromotionDetails
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.ui.mvi.collectAsState

@Composable
fun AllPromotionsEntry() = NavigationEntry<AllPromotionsFlowViewModel> {
    val viewState by viewModel.collectAsState()
    val lazyListState = rememberLazyListState()

    AllPromotionsScreen(
        viewModel = viewModel,
        viewState = viewState,
        lazyListState = lazyListState
    )

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                AllPromotionsFlowViewModel.AllPromotionsEvent.ScrollTop -> {
                    lazyListState.animateScrollToItem(0)
                }

                is AllPromotionsFlowViewModel.AllPromotionsEvent.GoToProductDetails -> {
                    navigator.navigateToPromotionDetails(event.promotionId)
                }

                AllPromotionsFlowViewModel.AllPromotionsEvent.GoBack -> {
                    navigator.goBack()
                }
            }
        }
    }
}
