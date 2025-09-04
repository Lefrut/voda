package com.vodovoz.app.feature.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vodovoz.app.design_system.composables.scaffold.VodovozScaffold
import com.vodovoz.app.feature.all.promotions.composables.AdvertisingInfoBottomSheet
import com.vodovoz.app.feature.catalog.composables.CatalogBody
import com.vodovoz.app.feature.catalog.composables.CatalogLoadingPlaceholder
import com.vodovoz.app.feature.home.composables.HomeTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(viewModel: CatalogFlowViewModel, viewState: CatalogFlowViewModel.CatalogState) {
    VodovozScaffold(
        topBar = {
            HomeTopBar(
                onFocus = {
                    viewModel.navigateToSearch()
                },
                onMicClick = {
                    viewModel.showSpeechRecognizer()
                },
                onScanClick = {
                    viewModel.navigateToScanner()
                },
                onSearchClick = {
                    viewModel.navigateToSearch()
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.padding(paddingValues)
        ) {
            when (viewState.uiState) {
                CatalogFlowViewModel.CatalogUiState.Success -> {
                    CatalogBody(
                        categories = viewState.categories,
                        banners = viewState.banners,
                        onCategoryClick = { catalogCategory ->
                            viewModel.chooseCategory(catalogCategory)
                        },
                        onBannerClick = { banner ->
                            viewModel.activateBannerAction(banner)
                        },
                        onAboutAdvertisingClick = {
                            viewModel.showAdvertisingBottomSheet(it)
                        }
                    )
                }

                else -> {
                    CatalogLoadingPlaceholder()
                }
            }
        }
    }

    if (viewState.showAdvertisingBS) {
        AdvertisingInfoBottomSheet(
            advertising = viewState.currentAdvertising,
            onDismissRequest = {
                viewModel.closeAdvertisingBottomSheet()
            }
        )
    }
}