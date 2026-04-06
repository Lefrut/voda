package com.m.vodovoz.feature.categories

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.feature.categories.api.CategoriesNavKey
import com.m.vodovoz.feature.categories.model.CategoriesEvent
import com.m.vodovoz.feature.categories.model.CategoriesState
import com.m.vodovoz.feature.home.model.CategoryUi
import com.m.vodovoz.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CategoriesViewModel.Factory::class)
@Stable
class CategoriesViewModel @AssistedInject constructor(
    val tabManager: TabManager,
    @Assisted private val navKey: CategoriesNavKey,
) : MviViewModel<CategoriesState, CategoriesEvent>(CategoriesState()) {

    private val categoriesArg = navKey.categoryList
    private val categoryArg = navKey.category

    init {
        setInitialCategories()
    }


    private fun setInitialCategories() = viewModelScope.launch {
        updateState { s ->
            s.copy(
                currentCategory = categoryArg,
                categories = categoriesArg.toList(),
                showApplyButton = false
            )
        }
    }


    fun selectCategory(category: CategoryUi) = viewModelScope.launch {
        updateState { s -> s.copy(currentCategory = category, showApplyButton = true) }
    }

    fun navigateBackWithArgs() = viewModelScope.launch {
        if(stateSnapshot.currentCategory == CategoryUi.Empty) return@launch

        sendEvent(CategoriesEvent.GoBackWithArguments(stateSnapshot.currentCategory))
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(CategoriesEvent.GoBack)
    }

    @AssistedFactory
    interface Factory {
        fun create(navKey: CategoriesNavKey): CategoriesViewModel
    }

}
