package com.m.vodovoz.feature.categories.model

import com.m.vodovoz.feature.home.model.CategoryUi
import com.m.vodovoz.feature.home.model.PopularCategoryUi

sealed interface CategoriesEvent {

    data class GoBackWithArguments(val currentCategory: CategoryUi) : CategoriesEvent

    data object GoBack: CategoriesEvent

}