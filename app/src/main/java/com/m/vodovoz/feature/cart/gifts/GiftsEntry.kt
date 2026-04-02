package com.m.vodovoz.feature.cart.gifts

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.ViewModelStoreOwner
import com.m.vodovoz.R
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.feature.cart.CartFlowViewModel
import com.m.vodovoz.design_system.composables.dialogs.VodovozDialog
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.cart.composables.ImagePreviewDialog
import com.m.vodovoz.feature.cart.gifts.api.GiftsNavKey
import com.m.vodovoz.feature.cart.gifts.model.GiftsEvent
import com.m.vodovoz.ui.mvi.collectAsState

@Composable
fun GiftsEntry(navKey: GiftsNavKey? = null) =
    NavigationEntry<GiftsViewModel, GiftsViewModel.Factory>(
        creationCallback = { factory -> factory.create(navKey) }
    ) {
    val activityOwner = LocalActivity.current as? ViewModelStoreOwner
    val cartViewModel = activityOwner?.let { hiltViewModel<CartFlowViewModel>(it) }
    val viewState by viewModel.collectAsState()

    LifecycleStartEffect(Unit) {
        viewModel.tabManager.setTabVisibility(false)
        onStopOrDispose {
            viewModel.tabManager.setTabVisibility(true)
        }
    }

    GiftsScreen(viewModel = viewModel, viewState = viewState)

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
                GiftsEvent.GoBack -> {
                    navigator.goBack()
                }

                is GiftsEvent.GoToCart -> {
                    cartViewModel?.addGiftToCart(event.currentGift)
                    navigator.goBack()
                }
            }
        }
    }
}
