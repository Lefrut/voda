package com.m.vodovoz.feature.categories.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.feature.home.model.CategoryUi
import com.m.vodovoz.feature.home.model.PopularCategoryUi

@Immutable
data class CategoriesState(
    val currentCategory: CategoryUi = CategoryUi.Empty.copy(name = "isn't name"),
    val categories: List<CategoryUi> = emptyList(),
    val showApplyButton: Boolean = false
)
