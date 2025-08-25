package com.vodovoz.app.feature.home.model

import androidx.compose.runtime.Stable
import com.vodovoz.app.design_system.model.BannerUi
import com.vodovoz.app.design_system.model.CategoryWithProductsUi
import com.vodovoz.app.design_system.model.ForAdultsUi
import com.vodovoz.app.design_system.model.ProductUi
import com.vodovoz.app.design_system.model.PromotionUi
import com.vodovoz.app.design_system.model.SectionUi
import com.vodovoz.app.design_system.model.StoryUi
import com.vodovoz.app.design_system.model.VodovozItemUi
import com.vodovoz.app.design_system.model.VodovozSectionUi

@Stable
sealed class HomeListItem<out T>(open val position: Int): VodovozItemUi<HomeListItem<T>>() {

    abstract val value: T

    override fun copyItem(
        forAdults: ForAdultsUi?,
        cartLoading: Boolean,
        isFavorite: Boolean,
        cartQuantity: Int,
        items: List<VodovozItemUi<*>>
    ): HomeListItem<T> { return this }

    data class Banner(override val value: List<BannerUi>) :
        HomeListItem<List<BannerUi>>(Positions.BANNER) 

    data class Stories(override val value: List<StoryUi>) :
        HomeListItem<List<StoryUi>>(Positions.STORIES)

    data class OrderWithMenu(override val value: OrderWithMenuUi) :
        HomeListItem<OrderWithMenuUi>(Positions.ORDER)

    data class PopularCategories(override val value: SectionUi<PopularCategoryUi>) :
        HomeListItem<SectionUi<PopularCategoryUi>>(Positions.POPULAR_CATEGORIES)

    data class Divider(
        override val position: Int,
        override val value: Any = Any()
    ) : HomeListItem<Any>(position)

    data class Promotions(override val value: SectionUi<PromotionUi>) :
        HomeListItem<SectionUi<PromotionUi>>(Positions.PROMOTIONS)

    sealed class Products<out T : VodovozItemUi<T>>(
        override val position: Int,
        override val value: T,
    ) : HomeListItem<T>(position) {

        data class Section(
            override val position: Int,
            override val value: VodovozSectionUi<ProductUi>,
        ) : Products<VodovozSectionUi<ProductUi>>(position, value){

            override val items: List<VodovozItemUi<*>>
                get() = value.items

            override fun copyItem(
                forAdults: ForAdultsUi?,
                cartLoading: Boolean,
                isFavorite: Boolean,
                cartQuantity: Int,
                items: List<VodovozItemUi<*>>
            ): Section {
                return copy(value = value.copyItem(items = items)) 
            }
        }

        data class CategoryWithProductsSection(
            val currentCategoryId: Long,
            override val position: Int,
            override val value: VodovozSectionUi<CategoryWithProductsUi>,
        ) : Products<VodovozSectionUi<CategoryWithProductsUi>>(position, value){

            override val items: List<VodovozItemUi<*>>
                get() = value.items

            override fun copyItem(
                forAdults: ForAdultsUi?,
                cartLoading: Boolean,
                isFavorite: Boolean,
                cartQuantity: Int,
                items: List<VodovozItemUi<*>>
            ): CategoryWithProductsSection {
                return copy(value = value.copyItem(items = items))
            }

        }

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
                    value = section
                )

            fun bottomSection(section: VodovozSectionUi<CategoryWithProductsUi>) =
                CategoryWithProductsSection(
                    currentCategoryId = section.items.firstOrNull()?.id ?: -1,
                    position = Positions.BOTTOM_SECTION,
                    value = section
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

inline fun <reified T : HomeListItem<*>> List<HomeListItem<*>>.getOrNull(): T? {
    return firstOrNull { item -> item is T } as? T
}

inline fun <VALUE, reified T : HomeListItem<VALUE>> List<HomeListItem<*>>.getValueOrNull(): VALUE? {
    return getOrNull<T>()?.value
}
