package com.vodovoz.app.feature.about_product

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.button.ProductBottomFloatingButton
import com.vodovoz.app.design_system.composables.tab_row.TabTitle
import com.vodovoz.app.design_system.composables.tab_row.VodovozTabRow
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.design_system.model.ProductDetailsTabUi
import com.vodovoz.app.feature.about_product.composables.CharacteristicsTabContent
import com.vodovoz.app.feature.about_product.composables.DescriptionTabContent
import com.vodovoz.app.feature.about_product.composables.DocumentsTabContent
import com.vodovoz.app.feature.about_product.model.AboutProductState

@Composable
fun AboutProductScreen(
    viewState: AboutProductState,
    viewModel: AboutProductViewModel,
) {

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
        topBar = {
            VodovozTopBar(
                onBack = { viewModel.navigateBack() },
                title = stringResource(id = R.string.about_product)
            )
        },
        bottomBar = {
            ProductBottomFloatingButton(
                isLoading = viewState.buttonIsLoading,
                cartQuantity = viewState.cartQuantity,
                totalPrice = viewState.productTotalPrice,
                oldPrice = viewState.productOldPrice,
                price = viewState.productPrice,
                giftText = viewState.presentHtml,
                isAvailable = viewState.productAvailable,
                analogButton = viewState.analogButton,
                contentPadding = PaddingValues(top = 10.dp, bottom = 16.dp),
                onIncrementProduct = {
                    viewModel.incrementProductToCart()
                },
                onDecrementProduct = {
                    viewModel.decrementProductFromCart()
                },
                onAnalogClick = {
                    viewModel.navigateToProductAnalogs()
                }
            )

        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            VodovozTabRow(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(horizontal = 16.dp),
                selectedTabPosition = viewState.selectedTabIndex
            ) {
                viewState.tabs.forEachIndexed { i, tab ->
                    TabTitle(
                        title = tab.title,
                        position = i,
                        selected = viewState.selectedTabIndex == i,
                        onClick = { viewModel.selectTab(i) }
                    )
                }
            }

            val currentTab =
                viewState.tabs.getOrElse(viewState.selectedTabIndex) { ProductDetailsTabUi("", "") }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                AnimatedContent(
                    targetState = currentTab.dataId,
                    transitionSpec = {
                        fadeIn(tween()) togetherWith fadeOut()
                    },
                    label = "TabSwitchAnimation"
                ) { tabId ->
                    when (tabId) {
                        viewState.description.id -> DescriptionTabContent(description = viewState.description)
                        viewState.characteristics.id -> CharacteristicsTabContent(characteristics = viewState.characteristics)
                        viewState.documents.id -> DocumentsTabContent(
                            documentsBlock = viewState.documents,
                            onDocumentClick = { document ->
                                viewModel.navigateToDocumentViewer(document)
                            }
                        )

                        else -> {}
                    }
                }
            }
        }

    }

}