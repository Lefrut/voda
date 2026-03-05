package com.m.vodovoz.feature.promotion_details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToProductAnalogs
import com.m.vodovoz.core.navigation.navigateToProductDetails
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.util.extensions.openUrl

@Composable
fun PromotionDetailsEntry() = NavigationEntry<PromotionDetailsViewModel> {
    val viewState by viewModel.collectAsState()
    val context = LocalContext.current

    when (viewState.uiState) {
        PromotionDetailsViewModel.UiState.Error -> {
            NetworkErrorPlaceholder(
                onTryAgainClick = { viewModel.fetchPromotionDetails() }
            )
        }

        else -> {
            PromotionDetailsScreen(
                viewModel = viewModel,
                viewState = viewState,
            )
        }
    }

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                PromotionDetailsViewModel.PromotionDetailEvent.GoBack -> {
                    navController.popBackStack()
                }

                is PromotionDetailsViewModel.PromotionDetailEvent.OpenUrl -> {
                    context.openUrl(event.url)
                }

                is PromotionDetailsViewModel.PromotionDetailEvent.GoToProductAnalogs -> {
                    navController.navigateToProductAnalogs(event.productId)
                }

                is PromotionDetailsViewModel.PromotionDetailEvent.GoToProductDetails -> {
                    navController.navigateToProductDetails(event.productId)
                }
            }
        }
    }
}
