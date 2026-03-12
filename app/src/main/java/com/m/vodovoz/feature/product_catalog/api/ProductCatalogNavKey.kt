package com.m.vodovoz.feature.product_catalog.api

import androidx.navigation3.runtime.NavKey
import com.m.vodovoz.feature.product_catalog.ProductCatalogFragment

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
    }

    fun toLegacy(): ProductCatalogFragment.DataSource =
        when (dataSource) {
            is DataSource.ButtonId -> ProductCatalogFragment.DataSource.ButtonProducts(dataSource.buttonId)
            is DataSource.BrandId -> ProductCatalogFragment.DataSource.Brand(dataSource.brandId)
            is DataSource.BannerProducts -> ProductCatalogFragment.DataSource.Products(
                dataSource.bannerId,
                dataSource.blockId
            )

            is DataSource.CategoryId -> ProductCatalogFragment.DataSource.Category(dataSource.categoryId)
            is DataSource.SearchQuery -> ProductCatalogFragment.DataSource.Search(dataSource.query)
            DataSource.HurryBuyUpProducts -> ProductCatalogFragment.DataSource.HurryBuyUpProducts
            DataSource.NewProducts -> ProductCatalogFragment.DataSource.NewProducts
            DataSource.ViewedProducts -> ProductCatalogFragment.DataSource.ViewedProducts
            DataSource.PastPurchases -> ProductCatalogFragment.DataSource.PastPurchases
        }

    companion object {
        const val NAV_NAME: String = "feature/product_catalog/ProductCatalog"
    }
}
