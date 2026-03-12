package com.m.vodovoz.feature.cart.gifts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleStartEffect
import com.m.vodovoz.R
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.design_system.composables.dialogs.VodovozDialog
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.cart.composables.ImagePreviewDialog
import com.m.vodovoz.feature.cart.gifts.model.GiftsEvent
import com.m.vodovoz.ui.mvi.collectAsState

@Composable
fun GiftsEntry() = NavigationEntry<GiftsViewModel> {
    val viewState by viewModel.collectAsState()

    LifecycleStartEffect(Unit) {
        viewModel.tabManager.changeTabVisibility(false)
        onStopOrDispose {
            viewModel.tabManager.changeTabVisibility(true)
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
                    navigator.previousBackStackEntry?.savedStateHandle?.set(
                        "gift",
                        event.currentGift
                    )
                    navigator.goBack()
                }
            }
        }
    }
}
