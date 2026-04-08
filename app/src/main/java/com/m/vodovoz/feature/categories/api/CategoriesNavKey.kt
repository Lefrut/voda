package com.m.vodovoz.feature.categories.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import com.m.vodovoz.core.navigation.viewmodel.SharedViewModelStoreNavKey
import com.m.vodovoz.feature.home.model.CategoryUi

data class CategoriesNavKey(
    val categoryList: Array<CategoryUi>,
    val category: CategoryUi,
    val source: Source,
    override val parentContentKey: String? = null,
) : VodovozNavKey, SharedViewModelStoreNavKey {
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
