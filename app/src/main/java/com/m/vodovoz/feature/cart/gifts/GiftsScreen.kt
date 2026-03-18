package com.m.vodovoz.feature.cart.gifts

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.m.vodovoz.R
import com.m.vodovoz.feature.cart.composables.SelectableProductsScreen
import com.m.vodovoz.feature.cart.gifts.model.GiftsState

@Composable
fun GiftsScreen(viewModel: GiftsViewModel, viewState: GiftsState) {
    SelectableProductsScreen(
        title = stringResource(id = R.string.choose_present),
        items = viewState.items,
        selectedItems = listOf(viewState.currentGift),
        button = viewState.button,
        present = viewState.present?.popupWindow?.present,
        purchase = false,
        onBackClick = viewModel::navigateBack,
        onItemClick = viewModel::selectGift,
        onImageClick = { gift -> viewModel.showPreviewImageDialog(gift.image) },
        onButtonClick = viewModel::tryToChooseGift
    )
}
