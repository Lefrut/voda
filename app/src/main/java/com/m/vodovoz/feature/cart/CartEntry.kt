package com.m.vodovoz.feature.cart

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.compose.LifecycleStartEffect
import com.m.vodovoz.R
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.LocalNavigator
import com.m.vodovoz.core.navigation.navigateToAllBottles
import com.m.vodovoz.core.navigation.navigateToCatalog
import com.m.vodovoz.core.navigation.navigateToGifts
import com.m.vodovoz.core.navigation.navigateToOrdering
import com.m.vodovoz.core.navigation.navigateToProfile
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
) {
    val viewState by viewModel.collectAsState()
    val view = LocalView.current
    val navigator = LocalNavigator.current
    val tabManager = viewModel.tabManager

    LifecycleEffect(tabManager) {
        tabManager.observeTabReselect().collect { id ->
            if (id != TabManager.DEFAULT_STATE && id == R.id.cartFragment) {
                tabManager.setDefaultState()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.accountManager.reportEvent("Зашел в корзину")
    }

    LifecycleStartEffect(Unit) {
        onStopOrDispose {
            tabManager.setTabVisibility(true)
        }
    }

    DisposableEffect(view, tabManager) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            tabManager.setTabVisibility(!imeVisible)
            insets
        }
        onDispose {
            ViewCompat.setOnApplyWindowInsetsListener(view, null)
        }
    }

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
                navigator.navigateToOrdering(event.coupon)
            }

            is CartFlowViewModel.CartEvents.GoToGifts -> {
                navigator.navigateToGifts(
                    event.present,
                    event.popupWindow
                )
            }

            is CartFlowViewModel.CartEvents.GoToProfile -> {
                navigator.navigateToProfile(tabManager)
            }

            is CartFlowViewModel.CartEvents.GoToProductDetails -> {
                navigator.navigateToProductDetails(event.productId)
            }

            CartFlowViewModel.CartEvents.GoToCatalog -> {
                navigator.navigateToCatalog()
            }

            is CartFlowViewModel.CartEvents.GoToAllBottles -> {
                navigator.navigateToAllBottles(event.bottles)
            }

            is CartFlowViewModel.CartEvents.GoToAnalogs -> {
                navigator.navigateToProductAnalogs(event.productId)
            }
        }
    }

    LaunchedEffect(viewState.showPromotionCodeBottomSheet) {
        if (viewState.showPromotionCodeBottomSheet) tabManager.setTabVisibility(false)
        else {
            delay(145)
            tabManager.setTabVisibility(true)
        }
    }
}
