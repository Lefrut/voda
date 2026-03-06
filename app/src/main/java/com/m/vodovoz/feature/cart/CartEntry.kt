package com.m.vodovoz.feature.cart

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.findNavController
import com.m.vodovoz.R
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.navigateToAllBottles
import com.m.vodovoz.core.navigation.navigateToGifts
import com.m.vodovoz.core.navigation.navigateToOrdering
import com.m.vodovoz.core.navigation.navigateToProductAnalogs
import com.m.vodovoz.core.navigation.navigateToProductDetails
import com.m.vodovoz.design_system.composables.placeholders.ForAdultsPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.VodovozPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.ui.mvi.collectEvents
import kotlinx.coroutines.delay

@Composable
fun CartEntry(
    viewModel: CartFlowViewModel,
    tabManager: TabManager,
) {
    val viewState by viewModel.collectAsState()
    val navController = LocalView.current.findNavController()

    when (val uiState = viewState.uiState) {
        CartFlowViewModel.CartUiState.Cart -> {
            val forAdultsUi = viewState.forAdultsUi
            if (forAdultsUi != null) {
                ForAdultsPlaceholder(
                    forAdults = forAdultsUi,
                    onBackClick = viewModel::closeForAdultsPlaceholder,
                    onApplyClick = viewModel::setCanViewForAdults
                )

                BackHandler {
                    viewModel.closeForAdultsPlaceholder()
                }

            } else CartScreen(
                viewModel = viewModel,
                viewState = viewState
            )
        }

        is CartFlowViewModel.CartUiState.Empty -> {
            val placeholder = uiState.placeholder
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                Text(
                    text = placeholder.title.ifEmpty { stringResource(R.string.cart) },
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )

                VodovozPlaceholder(
                    data = placeholder,
                    onButtonClick = { viewModel.navigateToCatalog() },
                    onProductLike = viewModel::changeFavorite,
                    onProductClick = viewModel::navigateToProductDetails,
                    onDecrementToCart = viewModel::decrementProduct,
                    onIncrementToCart = viewModel::incrementProduct,
                    onAnalogsClick = viewModel::navigateToAnalogsOrShow18
                )
            }
        }

        CartFlowViewModel.CartUiState.Error -> {
            NetworkErrorPlaceholder { viewModel.fetchCartDetails() }
        }

        CartFlowViewModel.CartUiState.Loading -> {
            LoadingPlaceholder()
        }
    }


    viewModel.collectEvents { event ->
        when (event) {
            is CartFlowViewModel.CartEvents.GoToOrder -> {
                if (navController.currentBackStackEntry?.destination?.id == R.id.orderingFragment) {
                    navController.popBackStack()
                }
                navController.navigateToOrdering(event.coupon)
            }

            is CartFlowViewModel.CartEvents.GoToGifts -> {
                navController.navigateToGifts(
                    event.present,
                    event.popupWindow
                )
            }

            is CartFlowViewModel.CartEvents.GoToProfile -> {
                tabManager.setAuthRedirect(navController.graph.id)
                tabManager.selectTab(R.id.graph_profile)
            }

            is CartFlowViewModel.CartEvents.GoToProductDetails -> {
                navController.navigateToProductDetails(event.productId)
            }

            CartFlowViewModel.CartEvents.GoToCatalog -> {
                tabManager.selectTab(R.id.graph_catalog)
            }

            is CartFlowViewModel.CartEvents.GoToAllBottles -> {
                navController.navigateToAllBottles(event.bottles)
            }

            is CartFlowViewModel.CartEvents.GoToAnalogs -> {
                navController.navigateToProductAnalogs(event.productId)
            }
        }
    }

    LaunchedEffect(viewState.showPromotionCodeBottomSheet) {
        if (viewState.showPromotionCodeBottomSheet) tabManager.changeTabVisibility(false)
        else {
            delay(145)
            tabManager.changeTabVisibility(true)
        }
    }
}
