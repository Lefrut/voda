package com.vodovoz.app.feature.categories

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.feature.categories.model.CategoriesEvent
import com.vodovoz.app.feature.categories.model.CategoriesState
import com.vodovoz.app.feature.home.model.CategoryUi
import com.vodovoz.app.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class CategoriesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : MviViewModel<CategoriesState, CategoriesEvent>(CategoriesState()) {

    private val categoriesArg =
        savedStateHandle.remove<Array<CategoryUi>>("categoryList") ?: emptyArray()
    private val categoryArg =
        savedStateHandle.remove<CategoryUi>("category") ?: categoriesArg.firstOrNull()
        ?: CategoryUi.Empty

    init {
        setInitialCategories()
    }


    private fun setInitialCategories() = viewModelScope.launch {
        _state.update { s ->
            s.copy(
                currentCategory = categoryArg,
                categories = categoriesArg.toList(),
                showApplyButton = false
            )
        }
    }


    fun selectCategory(category: CategoryUi) = viewModelScope.launch {
        _state.update { s -> s.copy(currentCategory = category, showApplyButton = true) }
    }

    fun navigateBackWithArgs() = viewModelScope.launch {
        if(stateSnapshot.currentCategory == CategoryUi.Empty) return@launch

        sendEvent(CategoriesEvent.GoBackWithArguments(stateSnapshot.currentCategory))
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(CategoriesEvent.GoBack)
    }


}