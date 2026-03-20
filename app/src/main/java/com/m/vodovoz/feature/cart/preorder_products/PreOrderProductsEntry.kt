package com.m.vodovoz.feature.cart.preorder_products

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.m.vodovoz.R
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.PRE_ORDER_PRODUCTS_STATE_KEY
import com.m.vodovoz.core.navigation.navigateToOrdering
import com.m.vodovoz.design_system.composables.dialogs.VodovozDialog
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.cart.composables.ImagePreviewDialog
import com.m.vodovoz.feature.cart.preorder_products.model.PreOrderProductsEvent
import com.m.vodovoz.ui.mvi.collectAsState

@Composable
fun PreOrderProductsEntry() = NavigationEntry<PreOrderProductsViewModel> {
    val viewState by viewModel.collectAsState()

    PreOrderProductsScreen(viewModel = viewModel, viewState = viewState)

    BackHandler {
        viewModel.navigateBack()
    }

    val previewImage = viewState.previewImage
    if (previewImage != null) {
        ImagePreviewDialog(
            image = previewImage,
            onDismissRequest = viewModel::closePreviewImageDialog
        )
    }

    if (viewState.showForAdultsDialog) {
        with(viewState.forAdultsDialog) {
            VodovozDialog(
                title = title,
                description = description,
                acceptButtonText = button.name,
                cancelButtonText = stringResource(id = R.string.no),
                onDismiss = {
                    viewModel.closeForAdultsDialog()
                },
                onAccept = {
                    viewModel.acceptForAdults()
                }
            )
        }
    }

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                is PreOrderProductsEvent.GoToOrdering -> {
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        PRE_ORDER_PRODUCTS_STATE_KEY,
                        ArrayList(event.productsInCart)
                    )
                    navController.popBackStack(R.id.cartFragment, false)
                    navController.navigateToOrdering(event.coupon)
                }

                is PreOrderProductsEvent.BackToCart -> {
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        PRE_ORDER_PRODUCTS_STATE_KEY,
                        ArrayList(event.productsInCart)
                    )
                    navController.popBackStack(R.id.cartFragment, false)
                }
            }
        }
    }
}
