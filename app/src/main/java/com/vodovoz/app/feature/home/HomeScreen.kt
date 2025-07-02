package com.vodovoz.app.feature.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.dialogs.VodovozDialog
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.feature.all.promotions.composables.AdvertisingInfoBottomSheet
import com.vodovoz.app.feature.home.composables.HomeBody
import com.vodovoz.app.feature.home.composables.HomeLoadingPlaceholder
import com.vodovoz.app.feature.home.composables.HomeTopBar
import com.vodovoz.app.feature.home.composables.SpecialPromotionBottomSheet
import com.vodovoz.app.feature.home.composables.UnratedProductsBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewState: HomeFlowViewModel.HomeState,
    viewModel: HomeFlowViewModel,
    pullRefreshState: PullToRefreshState,
    topProductsLazyListState: LazyListState,
) {

    val showedUnratedProducts by rememberUpdatedState(newValue = viewState.showedUnratedProducts)

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

    Scaffold(
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            state = pullRefreshState,
            isRefreshing = viewState.showRefreshIndicator,
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = viewState.showRefreshIndicator,
                    state = pullRefreshState,
                    containerColor = MaterialTheme.colorScheme.background,
                    color = MaterialTheme.colorScheme.primary
                )
            },
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

                HomeFlowViewModel.HomeUiState.Success -> {
                    HomeBody(
                        modifier = Modifier.nestedScroll(homeNestedScrollConnection),
                        topProductsLazyListState = topProductsLazyListState,
                        banners = viewState.banners,
                        stories = viewState.stories,
                        sectionPromotions = viewState.sectionPromotions,
                        orderWithMenu = viewState.orderWithMenu,
                        sectionPopularCategories = viewState.sectionPopularCategories,
                        sectionNewProducts = viewState.sectionNewProducts,
                        sectionHurryUpBuyProducts = viewState.sectionHurryUpBuyProducts,
                        sectionTop = viewState.sectionTop,
                        sectionBottomProducts = viewState.sectionBottom,
                        sectionViewedProducts = viewState.sectionViewedProducts,
                        currentCategoryWithProducts = viewState.currentCategoryWithProducts,
                        onCategorySelect = { categoryWithProductsUi ->
                            viewModel.selectCategory(categoryWithProductsUi)
                        },
                        onMenuItemClick = { menuItem ->
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
                        onAllViewedProductsClick = {
                            viewModel.navigateToViewedProducts()
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
        key("SpecialPromotionBottomSheet") {
            SpecialPromotionBottomSheet(
                specialPromotionUi = viewState.specialPromotion,
                onDismissRequest = { viewModel.closeSpecialPromotionBottomSheet() },
                onButtonClick = { specialPromotion ->
                    viewModel.activateSpecialPromotionAction(specialPromotion.actionWithButton.action)
                }
            )
        }
    }

    val animatedUnratedAlpha =
        animateFloatAsState(
            targetValue = if (viewState.showUnratedProductsBS && !viewState.showedUnratedProducts) 1f else 0f,
            label = "animatedUnratedAlpha",
            animationSpec = tween(300, 0),
        )

    val showUnratedProductBottomSheet by remember {
        derivedStateOf { animatedUnratedAlpha.value > 0f }
    }

    if (showUnratedProductBottomSheet) {
        UnratedProductsBottomSheet(
            modifier = Modifier
                .zIndex(Float.MAX_VALUE)
                .graphicsLayer {
                    alpha = animatedUnratedAlpha.value
                },
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

}