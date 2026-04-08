package com.m.vodovoz.feature.sub_categories

import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.design_system.model.ParentCategoryUi
import com.m.vodovoz.feature.sub_categories.api.SubCategoriesNavKey
import com.m.vodovoz.feature.sub_categories.model.SubCategoriesEvent
import com.m.vodovoz.feature.sub_categories.model.SubCategoriesState
import com.m.vodovoz.ui.mvi.MviViewModel
import com.m.vodovoz.ui.mvi.launchInViewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = SubCategoriesViewModel.Factory::class)
class SubCategoriesViewModel @AssistedInject constructor(
    val tabManager: TabManager,
    @Assisted private val navKey: SubCategoriesNavKey,
) : MviViewModel<SubCategoriesState, SubCategoriesEvent>(SubCategoriesState()) {

    private val catalogCategoryArg = navKey.category

    init {
        setInitialCatalogCategory()
    }

    private fun setInitialCatalogCategory() {
        updateState { s ->
            s.copy(catalogCategory = catalogCategoryArg)
        }
    }

    fun chooseCatalogCategory(catalogCategory: ParentCategoryUi) = viewModelScope.launch {
        if (catalogCategory.childCategories.isNotEmpty()) {
            sendEvent(SubCategoriesEvent.GoToSubCategories(catalogCategory))
        } else if (catalogCategory.action != null) {
            sendEvent(SubCategoriesEvent.ActivateDataAllAction(catalogCategory.action))
        } else {
            sendEvent(SubCategoriesEvent.GoToProductList(catalogCategory.id))

        }
    }

    fun navigateToSearch() = viewModelScope.launch {
        sendEvent(SubCategoriesEvent.GoToSearch)
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(SubCategoriesEvent.GoBack)
    }

    fun chooseParentCatalogCategory(catalogCategory: ParentCategoryUi) = viewModelScope.launch {
        if (catalogCategory.action != null) {
            sendEvent(SubCategoriesEvent.ActivateDataAllAction(catalogCategory.action))
        } else {
            sendEvent(SubCategoriesEvent.GoToProductList(catalogCategory.id))
        }


    }

    fun changeSearchQuery(query: String) = viewModelScope.launch {
        updateState { s -> s.copy(searchQuery = query) }
    }

    fun navigateToSpeechRecognizer() = launchInViewModelScope {
        sendEvent(SubCategoriesEvent.GoToSpeechRecognizer)
    }

    fun navigateToScanner() = launchInViewModelScope {
        sendEvent(SubCategoriesEvent.GoToScanner)
    }

    @AssistedFactory
    interface Factory {
        fun create(navKey: SubCategoriesNavKey): SubCategoriesViewModel
    }


}
