package com.m.vodovoz.feature.search.qrcode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.LifecycleStartEffect
import coil3.compose.rememberAsyncImagePainter
import com.m.vodovoz.R
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToProductDetails
import com.m.vodovoz.core.navigation.navigateToSearchProductList
import com.m.vodovoz.design_system.composables.placeholders.EmptyResultPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.EmptyResultPlaceholderItem
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.ui.mvi.collectAsState

@Composable
fun QrCodeEntry() = NavigationEntry<QrCodeViewModel> {
    val viewState by viewModel.collectAsState()

    LifecycleStartEffect(Unit) {
        viewModel.tabManager.changeTabVisibility(false)
        onStopOrDispose {
            viewModel.tabManager.changeTabVisibility(true)
        }
    }

    when (val uiState = viewState.uiState) {
        is QrCodeViewModel.QrCodeUiState.EmptyResult -> {
            EmptyResultPlaceholder(
                title = uiState.title,
                description = uiState.description,
                item = EmptyResultPlaceholderItem.Cross,
                imagePainter = rememberAsyncImagePainter(
                    model = uiState.imageUrl,
                    error = painterResource(id = R.drawable.pic_search)
                ),
                onItemClick = {
                    viewModel.setScannerState()
                }
            )
        }

        QrCodeViewModel.QrCodeUiState.Scanner -> {
            ScannerScreen(
                viewState = viewState,
                viewModel = viewModel,
            )
        }
    }

    LifecycleEffect {
        viewModel.events.collect { qrCodeEvents ->
            when (qrCodeEvents) {
                is QrCodeViewModel.QrCodeEvents.Success -> {
                    navController.navigateToProductDetails(qrCodeEvents.id.toLong())
                }

                QrCodeViewModel.QrCodeEvents.GoBack -> {
                    navController.popBackStack()
                }

                is QrCodeViewModel.QrCodeEvents.GoToProductDetails -> {
                    navController.navigateToProductDetails(qrCodeEvents.id)
                }

                is QrCodeViewModel.QrCodeEvents.GoToSearchProducts -> {
                    navController.navigateToSearchProductList(qrCodeEvents.barCode)
                }
            }
        }
    }
}
