package com.vodovoz.app.feature.all.promotions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.feature.all.promotions.composables.AdvertisingInfoBottomSheet
import com.vodovoz.app.feature.all.promotions.composables.AllPromotionsBody
import com.vodovoz.app.feature.all.promotions.composables.PromotionsLoadingPlaceholder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllPromotionsScreen(
    viewModel: AllPromotionsFlowViewModel,
    viewState: AllPromotionsFlowViewModel.AllPromotionsState,
    lazyListState: LazyListState,
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        VodovozTopBar(
            onBack = { viewModel.navigateBack() },
            title = viewState.title
        )

        when (viewState.uiState) {
            AllPromotionsFlowViewModel.UiState.Error -> {
                NetworkErrorPlaceholder {
                    viewModel.fetchPromotions()
                }
            }
            AllPromotionsFlowViewModel.UiState.Loading -> {
                PromotionsLoadingPlaceholder()
            }

            AllPromotionsFlowViewModel.UiState.Success -> {
                AllPromotionsBody(
                    categories = viewState.categories,
                    currentCategory = viewState.currentCategory,
                    promotions = viewState.items,
                    lazyListState = lazyListState,
                    appendState = viewState.appendState,
                    onSectionSelect = { section ->
                        viewModel.selectSection(section)
                    },
                    onAdvertisingClick = { promotionUi ->
                        viewModel.showAdvertisingBottomSheet(promotionUi)
                    },
                    onPromotionClick = { promotion ->
                        viewModel.navigateToPromotionDetails(promotion)
                    },
                    onPromotionSee = { index ->
                        viewModel.notifyPaging(index)

                    }
                )
            }
        }

    }

    val bottomSheetState = rememberModalBottomSheetState()
    if (viewState.showAdvertisingBottomSheet) {
        AdvertisingInfoBottomSheet(
            advertising = viewState.currentAdvertising,
            onDismissRequest = { viewModel.closeAdvertisingBottomSheet() },
            state = bottomSheetState
        )
    }

}