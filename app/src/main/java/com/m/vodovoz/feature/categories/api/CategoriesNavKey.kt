package com.m.vodovoz.feature.categories.api

import androidx.navigation3.runtime.NavKey
import com.m.vodovoz.core.navigation.viewmodel.SharedViewModelStoreNavKey
import com.m.vodovoz.feature.home.model.CategoryUi

data class CategoriesNavKey(
    val categoryList: Array<CategoryUi>,
    val category: CategoryUi,
    val source: Source,
    override val parentContentKey: String? = null,
) : NavKey, SharedViewModelStoreNavKey {
    override fun toString(): String = parentContentKey
        ?: "CategoriesNavKey(categoryList=${categoryList.contentToString()}, category=$category, source=$source)"

    enum class Source {
        ProductCatalog,
        Favorite,
    }

    companion object {
        const val NAV_NAME: String = "feature/categories/Categories"
    }
}
