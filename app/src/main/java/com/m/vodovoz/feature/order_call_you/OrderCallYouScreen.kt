package com.m.vodovoz.feature.order_call_you

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.m.vodovoz.design_system.composables.button.VodovozButtonsColumn
import com.m.vodovoz.design_system.composables.floating.BottomFloatingContainer
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.composables.top_bar.VodovozTopBar
import com.m.vodovoz.feature.order_call_you.copmosables.OrderCallYouBody
import com.m.vodovoz.feature.order_call_you.model.OrderCallYouState
import com.m.vodovoz.feature.order_call_you.model.OrderCallYouUiState

@Composable
fun OrderCallYouScreen(
    viewModel: OrderCallYouViewModel,
    viewState: OrderCallYouState,
) {
    val uiState = viewState.uiState

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            VodovozTopBar(
                onBack = { viewModel.navigateBack() },
                title = viewState.title
            )
        },
        bottomBar = {
            if (uiState is OrderCallYouUiState.CallYou) {
                BottomFloatingContainer {
                    VodovozButtonsColumn(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        buttons = listOf(viewState.button),
                        onButtonClick = { button ->
                            viewModel.chooseOrderingCallYou(button)
                        }
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Box(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
            when (uiState) {
                OrderCallYouUiState.CallYou -> {
                    OrderCallYouBody(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            bottom = paddingValues.calculateBottomPadding()
                        ),
                        items = viewState.items,
                        currentItem = viewState.currentItem,
                        onItemSelect = { item ->
                            viewModel.selectCallYouItem(item)
                        }
                    )
                }

                OrderCallYouUiState.Error -> {
                    NetworkErrorPlaceholder {
                        viewModel.fetchOrderCallYouDetails()
                    }
                }

                OrderCallYouUiState.Loading -> {
                    LoadingPlaceholder()
                }
            }
        }
    }
}