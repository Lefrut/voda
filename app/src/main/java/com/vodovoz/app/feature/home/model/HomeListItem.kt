package com.vodovoz.app.feature.home.model

import androidx.compose.runtime.Stable
import com.vodovoz.app.design_system.model.BannerUi
import com.vodovoz.app.design_system.model.CategoryWithProductsUi
import com.vodovoz.app.design_system.model.ProductUi
import com.vodovoz.app.design_system.model.PromotionUi
import com.vodovoz.app.design_system.model.SectionUi
import com.vodovoz.app.design_system.model.StoryUi
import com.vodovoz.app.design_system.model.VodovozItemUi
import com.vodovoz.app.design_system.model.VodovozSectionUi

@Stable
sealed class HomeListItem(open val position: Int) {

    data class Banner(val value: List<BannerUi>) : HomeListItem(Positions.BANNER)
    data class Stories(val value: List<StoryUi>) : HomeListItem(Positions.STORIES)
    data class OrderWithMenu(val value: OrderWithMenuUi) : HomeListItem(Positions.ORDER)
    data class PopularCategories(val value: SectionUi<PopularCategoryUi>) :
        HomeListItem(Positions.POPULAR_CATEGORIES)

    data class Divider(override val position: Int): HomeListItem(position)

    data class Promotions(val value: SectionUi<PromotionUi>) :
        HomeListItem(Positions.PROMOTIONS)

    sealed class Products<out T : VodovozItemUi<T>>(
        override val position: Int,
        open val item: T,
    ) : HomeListItem(position) {

        data class Section(
            override val position: Int,
            override val item: VodovozSectionUi<ProductUi>,
        ) : Products<VodovozSectionUi<ProductUi>>(position, item)

        data class CategoryWithProductsSection(
            val currentCategoryId: Long,
            override val position: Int,
            override val item: VodovozSectionUi<CategoryWithProductsUi>,
        ) : Products<VodovozSectionUi<CategoryWithProductsUi>>(position, item)

        companion object Factory {
            fun hurryBuyUp(items: VodovozSectionUi<ProductUi>) =
                Section(Positions.HURRY_BUY_UP, items)

            fun newProducts(items: VodovozSectionUi<ProductUi>) =
                Section(Positions.NEW_PRODUCTS, items)

            fun viewedProducts(items: VodovozSectionUi<ProductUi>) =
                Section(Positions.VIEWED, items)

            fun topSection(section: VodovozSectionUi<CategoryWithProductsUi>) =
                CategoryWithProductsSection(
                    currentCategoryId = section.items.firstOrNull()?.id ?: -1,
                    position = Positions.TOP_SECTION,
                    item = section
                )

            fun bottomSection(section: VodovozSectionUi<CategoryWithProductsUi>) =
                CategoryWithProductsSection(
                    currentCategoryId = section.items.firstOrNull()?.id ?: -1,
                    position = Positions.BOTTOM_SECTION,
                    item = section
                )
        }
    }


    private object Positions {
        const val BANNER = 100
        const val STORIES = 200
        const val ORDER = 300
        const val POPULAR_CATEGORIES = 400
        const val TOP_SECTION = 500
        const val HURRY_BUY_UP = 600
        const val NEW_PRODUCTS = 700
        const val PROMOTIONS = 800
        const val BOTTOM_SECTION = 900
        const val VIEWED = 1000
    }

}
