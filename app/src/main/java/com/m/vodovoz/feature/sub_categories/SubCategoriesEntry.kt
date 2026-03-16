package com.m.vodovoz.feature.sub_categories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.activate
import com.m.vodovoz.core.navigation.navigateToCategoryProductList
import com.m.vodovoz.core.navigation.navigateToQrCode
import com.m.vodovoz.core.navigation.navigateToSearch
import com.m.vodovoz.core.navigation.navigateToSpeechDialog
import com.m.vodovoz.core.navigation.navigateToSubCategories
import com.m.vodovoz.feature.sub_categories.api.SubCategoriesNavKey
import com.m.vodovoz.feature.sub_categories.model.SubCategoriesEvent
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.ui.mvi.collectEvents

@Composable
fun SubCategoriesEntry(navKey: SubCategoriesNavKey? = null) =
    NavigationEntry<SubCategoriesViewModel, SubCategoriesViewModel.Factory>(
        creationCallback = { factory -> factory.create(navKey) }
    ) {
    val viewState by viewModel.collectAsState()

    SubCategoriesScreen(
        viewModel = viewModel,
        viewState = viewState
    )

    viewModel.collectEvents { event ->
        when (event) {
            is SubCategoriesEvent.GoToProductList -> {
                navigator.navigateToCategoryProductList(event.categoryId)
            }

            is SubCategoriesEvent.GoToSubCategories -> {
                navigator.navigateToSubCategories(event.category)
            }

            SubCategoriesEvent.GoBack -> {
                navigator.goBack()
            }

            SubCategoriesEvent.GoToSearch -> {
                navigator.navigateToSearch()
            }

            is SubCategoriesEvent.ActivateDataAllAction -> {
                event.action.activate(
                    navigator = navigator,
                    tabManager = viewModel.tabManager
                )
            }

            SubCategoriesEvent.GoToScanner -> {
                navigator.navigateToQrCode()
            }

            SubCategoriesEvent.GoToSpeechRecognizer -> {
                navigator.navigateToSpeechDialog()
            }
        }
    }
}
