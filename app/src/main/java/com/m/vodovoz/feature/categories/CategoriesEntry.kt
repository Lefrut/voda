package com.m.vodovoz.feature.categories

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.categories.api.CategoriesNavKey
import com.m.vodovoz.feature.categories.model.CategoriesEvent
import com.m.vodovoz.feature.favorite.FavoriteFlowViewModel
import com.m.vodovoz.feature.product_catalog.ProductCatalogViewModel
import com.m.vodovoz.ui.mvi.collectAsState

@Composable
fun CategoriesEntry(navKey: CategoriesNavKey? = null) =
    NavigationEntry<CategoriesViewModel, CategoriesViewModel.Factory>(
        creationCallback = { factory -> factory.create(navKey) }
    ) {
    val activityOwner = LocalActivity.current as? ViewModelStoreOwner
    val productCatalogViewModel = when (navKey?.source) {
        CategoriesNavKey.Source.ProductCatalog -> viewModel(modelClass = ProductCatalogViewModel::class)
        else -> null
    }
    val favoriteViewModel = when (navKey?.source) {
        CategoriesNavKey.Source.Favorite -> activityOwner?.let { hiltViewModel<FavoriteFlowViewModel>(it) }
        else -> null
    }
    val viewState by viewModel.collectAsState()

    LifecycleStartEffect(Unit) {
        viewModel.tabManager.setTabVisibility(false)
        onStopOrDispose {
            viewModel.tabManager.setTabVisibility(true)
        }
    }

    CategoriesScreen(viewModel = viewModel, viewState = viewState)

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                is CategoriesEvent.GoBackWithArguments -> {
                    productCatalogViewModel?.selectCategory(event.currentCategory)
                    favoriteViewModel?.selectCategory(event.currentCategory)
                    navigator.goBack()
                }

                CategoriesEvent.GoBack -> {
                    navigator.goBack()
                }
            }
        }
    }
}
