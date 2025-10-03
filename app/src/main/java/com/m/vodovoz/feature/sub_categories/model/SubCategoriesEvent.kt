package com.m.vodovoz.feature.sub_categories.model

import com.m.vodovoz.common.model.DataAllAction
import com.m.vodovoz.design_system.model.ParentCategoryUi

sealed interface SubCategoriesEvent {
    data object GoToSearch : SubCategoriesEvent
    data object GoBack : SubCategoriesEvent
    data object GoToSpeechRecognizer : SubCategoriesEvent
    data object GoToScanner : SubCategoriesEvent

    data class GoToProductList(val categoryId: Long) : SubCategoriesEvent

    data class GoToSubCategories(val category: ParentCategoryUi): SubCategoriesEvent
    data class ActivateDataAllAction(val action: DataAllAction) : SubCategoriesEvent


}