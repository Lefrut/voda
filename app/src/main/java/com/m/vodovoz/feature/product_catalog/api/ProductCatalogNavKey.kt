package com.m.vodovoz.feature.product_catalog.api

import androidx.navigation3.runtime.NavKey

data class ProductCatalogNavKey(
    val dataSource: DataSource,
) : NavKey {
    sealed interface DataSource {
        data class ButtonId(val buttonId: Int) : DataSource
        data class BrandId(val brandId: Long) : DataSource
        data class BannerProducts(val bannerId: Long, val blockId: Long) : DataSource
        data class CategoryId(val categoryId: Long) : DataSource
        data class SearchQuery(val query: String) : DataSource
        data object HurryBuyUpProducts : DataSource
        data object NewProducts : DataSource
        data object ViewedProducts : DataSource
        data object PastPurchases : DataSource
        data object Missing : DataSource
    }

    companion object {
        const val NAV_NAME: String = "feature/product_catalog/ProductCatalog"
    }
}
