package com.m.vodovoz.feature.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import com.m.vodovoz.R
import com.m.vodovoz.common.model.VodovozAction
import com.m.vodovoz.design_system.composables.dialogs.VodovozDialog
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.composables.pull_to_refresh.VodovozPullToRefreshBox
import com.m.vodovoz.design_system.composables.scaffold.VodovozScaffold
import com.m.vodovoz.feature.all.promotions.composables.AdvertisingInfoBottomSheet
import com.m.vodovoz.feature.home.composables.AppUpdateBottomSheet
import com.m.vodovoz.feature.home.composables.HomeBody
import com.m.vodovoz.feature.home.composables.HomeLoadingPlaceholder
import com.m.vodovoz.feature.home.composables.HomeTopBar
import com.m.vodovoz.feature.home.composables.SpecialPromotionBottomSheet
import com.m.vodovoz.feature.home.composables.UnratedProductsBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewState: HomeFlowViewModel.HomeState,
    viewModel: HomeFlowViewModel,
) {
    val showedUnratedProducts by rememberUpdatedState(
        newValue = viewState.showedUnratedProducts
    )

    val homeNestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (consumed.y < 0 && !showedUnratedProducts) {
                    viewModel.showUnratedProducts()
                }
                return super.onPostScroll(consumed, available, source)
            }
        }
    }

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
                    viewModel.navigateToQrCode()
                },
                onSearchClick = {
                    viewModel.navigateToSearch()
                }
            )
        },
    ) { paddingValues ->
        VodovozPullToRefreshBox(
            modifier = Modifier.padding(paddingValues),
            isRefreshing = viewState.showRefreshIndicator,
            onRefresh = {
                viewModel.refresh()
            }
        ) {
            when (viewState.uiState) {
                HomeFlowViewModel.HomeUiState.Loading -> {
                    HomeLoadingPlaceholder()
                }

                HomeFlowViewModel.HomeUiState.NetworkError -> {
                    NetworkErrorPlaceholder {
                        viewModel.fetchHomeDetails()
                    }
                }

                HomeFlowViewModel.HomeUiState.Success, is HomeFlowViewModel.HomeUiState.AppNeedUpdate -> {
                    HomeBody(
                        modifier = Modifier.nestedScroll(homeNestedScrollConnection),
                        items = viewState.items,
                        onCategorySelect = { item, categoryWithProductsId ->
                            viewModel.selectCategory(item, categoryWithProductsId)
                        },
                        onOrderMenuItemClick = { menuItem ->
                            viewModel.navigateByMenuItem(menuItem)
                        },
                        onOrderClick = { homeOrder ->
                            viewModel.navigateToOrderDetails(homeOrder)
                        },
                        onPopularCategoryClick = { popularCategory ->
                            viewModel.navigateToPopularCategory(popularCategory)
                        },
                        onStoryClick = { story ->
                            viewModel.navigateToStories(story)
                        },
                        onPromotionClick = { promotion ->
                            viewModel.navigateToPromotionDetails(promotion)
                        },
                        onProductCardClick = { product ->
                            viewModel.navigateToProductDetails(product)
                        },
                        onProductLike = { product ->
                            viewModel.changeFavorite(product)
                        },
                        onShowAllClick = { action ->
                            viewModel.handleButtonAction(action)
                        },
                        onAboutAdvertisingClick = { aboutAdvertisingUi ->
                            viewModel.showAdvertisingBottomSheet(aboutAdvertisingUi)
                        },
                        onBannerClick = { banner ->
                            viewModel.activateBannerAction(banner)
                        },
                        onIncrementProductToCart = { product ->
                            viewModel.incrementProductToCart(product)
                        },
                        onDecrementProductToCart = { product ->
                            viewModel.decrementProductToCart(product)
                        },
                        onProductAnalogsClick = { product ->
                            viewModel.navigateToProductAnalogs(product)
                        }
                    )
                }
            }

        }
    }


    if (viewState.showAdvertisingBS) {
        AdvertisingInfoBottomSheet(
            advertising = viewState.currentAdvertising,
            onDismissRequest = { viewModel.closeAdvertisingBottomSheet() }
        )
    }


    if (viewState.showSpecialPromotionBS) {
        val specialPromotionActionName = viewState.specialPromotion.action.actionName

        SpecialPromotionBottomSheet(
            state = rememberModalBottomSheetState(true) { sheetValue ->
                specialPromotionActionName != VodovozAction.Name.CLOSE || sheetValue != SheetValue.Hidden
            },
            specialPromotionUi = viewState.specialPromotion,
            onDismissRequest = { _ ->
                viewModel.closeSpecialPromotionBottomSheet()
            },
            onButtonClick = { specialPromotion ->
                viewModel.activateSpecialPromotionAction(specialPromotion.action)
            },
            onAboutAdvertisingClick = viewModel::showAdvertisingBottomSheet
        )
    }


    if (viewState.showUnratedProductsBS && !viewState.showedUnratedProducts) {
        UnratedProductsBottomSheet(
            modifier = Modifier.zIndex(Float.MAX_VALUE),
            sectionUnratedProducts = viewState.sectionUnratedProducts,
            onProductRatingChanged = { product, rating ->
                viewModel.navigateToWriteComment(product, rating)
            },
            onDispose = {
                viewModel.closeUnratedProductsBottomSheet()
            },
            onProductNoRateClick = { product ->
                viewModel.noRateProduct(product)
            }
        )
    }

    if (viewState.showExitDialog) {
        VodovozDialog(
            title = stringResource(id = R.string.exit_dialog_title),
            description = stringResource(id = R.string.exit_dialog_description),
            acceptButtonText = stringResource(id = R.string.exit),
            cancelButtonText = stringResource(id = R.string.cancel),
            onDismiss = { viewModel.hideExitDialog() },
            onAccept = { viewModel.closeApplication() }
        )
    }

    val uiState = viewState.uiState
    if (uiState is HomeFlowViewModel.HomeUiState.AppNeedUpdate) {
        AppUpdateBottomSheet(
            appUpdateInfoUi = uiState.info,
            onDismissRequest = {},
            onButtonClick = { appUpdateInfo ->
                viewModel.openGooglePlay(appUpdateInfo)
            }
        )

        BackHandler {}
    }

}