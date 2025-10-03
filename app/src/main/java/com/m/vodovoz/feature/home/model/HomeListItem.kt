package com.m.vodovoz.feature.home.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.m.vodovoz.design_system.model.BannerUi
import com.m.vodovoz.design_system.model.CategoryWithProductsUi
import com.m.vodovoz.design_system.model.ForAdultsUi
import com.m.vodovoz.design_system.model.ProductUi
import com.m.vodovoz.design_system.model.PromotionUi
import com.m.vodovoz.design_system.model.SectionUi
import com.m.vodovoz.design_system.model.StoryUi
import com.m.vodovoz.design_system.model.VodovozItemUi
import com.m.vodovoz.design_system.model.VodovozSectionUi

@Stable
sealed class HomeListItem<out T>(open val position: Int) : VodovozItemUi<HomeListItem<T>>() {

    abstract val value: T

    override fun copyItem(
        forAdults: ForAdultsUi?,
        cartLoading: Boolean,
        isFavorite: Boolean,
        cartQuantity: Int,
        items: List<VodovozItemUi<*>>,
    ): HomeListItem<T> {
        return this
    }

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
        override val value: Any = Any(),
    ) : HomeListItem<Any>(position)

    data class Promotions(override val value: SectionUi<PromotionUi>) :
        HomeListItem<SectionUi<PromotionUi>>(Positions.PROMOTIONS)

    sealed class Products<out T : VodovozItemUi<T>>(
        override val position: Int,
        override val value: T,
    ) : HomeListItem<T>(position) {

        override val items: List<VodovozItemUi<*>>
            get() = value.items

        data class Section(
            override val position: Int,
            override val value: VodovozSectionUi<ProductUi>,
        ) : Products<VodovozSectionUi<ProductUi>>(position, value) {

            override fun copyItem(
                forAdults: ForAdultsUi?,
                cartLoading: Boolean,
                isFavorite: Boolean,
                cartQuantity: Int,
                items: List<VodovozItemUi<*>>,
            ): Section {
                return copy(value = value.copyItem(items = items))
            }
        }

        data class CategoriesWithProductsSection(
            val currentCategoryId: Long,
            override val position: Int,
            override val value: VodovozSectionUi<CategoryWithProductsUi>,
        ) : Products<VodovozSectionUi<CategoryWithProductsUi>>(position, value) {

            override fun copyItem(
                forAdults: ForAdultsUi?,
                cartLoading: Boolean,
                isFavorite: Boolean,
                cartQuantity: Int,
                items: List<VodovozItemUi<*>>,
            ): CategoriesWithProductsSection {
                return copy(value = value.copyItem(items = items))
            }

        }

        companion object Factory {
            fun hurryBuyUp(items: VodovozSectionUi<ProductUi>): Products<*> =
                Section(Positions.HURRY_BUY_UP, items)

            fun newProducts(items: VodovozSectionUi<ProductUi>): Products<*> =
                Section(Positions.NEW_PRODUCTS, items)

            fun viewedProducts(items: VodovozSectionUi<ProductUi>): Products<*> =
                Section(Positions.VIEWED, items)

            fun topSection(section: VodovozSectionUi<CategoryWithProductsUi>): CategoriesWithProductsSection =
                CategoriesWithProductsSection(
                    currentCategoryId = section.items.firstOrNull()?.id ?: -1,
                    position = Positions.TOP_SECTION,
                    value = section
                )

            fun bottomSection(section: VodovozSectionUi<CategoryWithProductsUi>): CategoriesWithProductsSection =
                CategoriesWithProductsSection(
                    currentCategoryId = section.items.firstOrNull()?.id ?: -1,
                    position = Positions.BOTTOM_SECTION,
                    value = section
                )
        }
    }


    object Positions {
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

fun <T> List<HomeListItem<T>>.plusItem(item: HomeListItem<T>): List<HomeListItem<T>> {
    return (listOf(item) + this).distinctBy { it.position }
}

inline fun <reified T : HomeListItem<*>> List<HomeListItem<*>>.firstOrNull(
    position: Int? = null,
): T? {
    return firstOrNull { item ->
        item is T && (position == null || position == item.position)
    } as? T
}

inline fun <VALUE, reified T : HomeListItem<VALUE>> List<HomeListItem<*>>.firstValueOrNull(
    position: Int? = null,
): VALUE? {
    return firstOrNull<T>(position)?.value
}

inline fun List<HomeListItem<*>>.forEach(action: (HomeListItem<*>) -> Unit) {
    sortedBy { it.position }.forEach(action)
}


val HomeListItem.Products.CategoriesWithProductsSection.products
    get() = value.items.firstOrNull { it.id == currentCategoryId }?.items ?: emptyList()

fun HomeListItem.Products.CategoriesWithProductsSection.withProducts(
    products: List<ProductUi>,
): HomeListItem.Products.CategoriesWithProductsSection {
    return copy(
        value = value.withItems {
            map { categoryWithProducts ->
                if (categoryWithProducts.id == currentCategoryId) {
                    categoryWithProducts.copy(items = products)
                } else {
                    categoryWithProducts
                }
            }
        }
    )
}

@Composable
inline fun HomeItem(
    item: HomeListItem<*>,
    banner: @Composable HomeListItem.Banner.() -> Unit,
    divider: @Composable HomeListItem.Divider.() -> Unit,
    orderWithMenu: @Composable HomeListItem.OrderWithMenu.() -> Unit,
    popularCategories: @Composable HomeListItem.PopularCategories.() -> Unit,
    categoriesWithProductsSection: @Composable HomeListItem.Products.CategoriesWithProductsSection.() -> Unit,
    productsSection: @Composable HomeListItem.Products.Section.() -> Unit,
    promotions: @Composable HomeListItem.Promotions.() -> Unit,
    stories: @Composable HomeListItem.Stories.() -> Unit,
) {
    with(item) {
        when (this) {
            is HomeListItem.Banner -> if (value.isNotEmpty()) banner()
            is HomeListItem.Divider -> divider()
            is HomeListItem.OrderWithMenu -> if (value.order != null || value.menuItems.isNotEmpty()) orderWithMenu()
            is HomeListItem.PopularCategories -> if (value.items.isNotEmpty()) popularCategories()
            is HomeListItem.Products.CategoriesWithProductsSection -> if (value.items.isNotEmpty()) categoriesWithProductsSection()
            is HomeListItem.Products.Section -> if (value.items.isNotEmpty()) productsSection()
            is HomeListItem.Promotions -> if (value.items.isNotEmpty()) promotions()
            is HomeListItem.Stories -> if (value.isNotEmpty()) stories()
        }
    }
}

fun List<HomeListItem<*>>.homeListItemIterator(): HomeListItemIterator<HomeListItem<*>> {
    return HomeListItemIterator(this)
}



class HomeListItemIterator<T : HomeListItem<*>>(
    data: List<T>,
) : Iterator<T> {

    private val sortedList = data.sortedBy { it.position }
    var index: Int = 0

    override fun hasNext(): Boolean {
        return index <= sortedList.size - 1
    }

    override fun next(): T {
        TODO()
        return sortedList[++index]
    }

}


