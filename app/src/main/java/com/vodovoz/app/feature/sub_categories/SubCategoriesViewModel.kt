package com.vodovoz.app.feature.sub_categories

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.design_system.model.ParentCategoryUi
import com.vodovoz.app.feature.sub_categories.model.SubCategoriesEvent
import com.vodovoz.app.feature.sub_categories.model.SubCategoriesState
import com.vodovoz.app.ui.mvi.MviViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SubCategoriesViewModel(
    savedStateHandle: SavedStateHandle,
) : MviViewModel<SubCategoriesState, SubCategoriesEvent>(SubCategoriesState()) {

    private val catalogCategoryArg =
        savedStateHandle.get<ParentCategoryUi>("category") ?: ParentCategoryUi.Empty

    init {
        setInitialCatalogCategory()
    }

    private fun setInitialCatalogCategory() {
        _state.update { s ->
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
        _state.update { s -> s.copy(searchQuery = query) }
    }


}