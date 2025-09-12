package com.vodovoz.app.design_system.model

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.navigation.findNavController
import com.vodovoz.app.R
import com.vodovoz.app.common.model.ButtonAction
import com.vodovoz.app.core.navigation.navigateToPreOrder
import com.vodovoz.app.design_system.ExtendedTheme
import com.vodovoz.app.design_system.composables.button.QuantityButtonSmall
import com.vodovoz.app.design_system.composables.button.VodovozButtonDefaults
import com.vodovoz.app.design_system.composables.button.VodovozButtonSmall
import com.vodovoz.app.domain.general.model.product.ButtonModel
import com.vodovoz.app.domain.general.model.product.CategoryWithProductsModel
import com.vodovoz.app.domain.general.model.product.PopularCategoryModel
import com.vodovoz.app.domain.general.model.product.ProductModel
import com.vodovoz.app.domain.general.model.product.SectionModel
import com.vodovoz.app.domain.general.model.promotion.LabelModel
import com.vodovoz.app.feature.home.model.PopularCategoryUi
import com.vodovoz.app.feature.home.model.toUi
import com.vodovoz.app.ui.graphics.fromHexOrUnspecified


fun <T : VodovozItemUi<T>> Iterable<T>.withCanViewForAdults(canView: Boolean): List<T> {
    if (!canView) return toList()
    return map { vodovozItem ->
        vodovozItem.copyItem(
            forAdults = null,
            items = vodovozItem.items.map { item ->
                item.withCanViewForAdults()
            }
        )
    }
}

fun VodovozItemUi<*>.withCanViewForAdults(): VodovozItemUi<*> {
    val updatedItems = items.map { it.withCanViewForAdults() }
    return copyItem(forAdults = null, items = updatedItems)
}


fun <T : VodovozItemUi<T>> Iterable<T>.withUpdatedCartRecursive(
    cart: Map<Long, Int>,
): List<T> =
    map { vodovozItem ->
        vodovozItem.copyItem(
            cartQuantity = cart.getOrDefault(vodovozItem.id, 0),
            items = vodovozItem.items.map { item ->
                item.withUpdatedCartRecursive(cart)
            }
        )
    }

fun VodovozItemUi<*>.withUpdatedCartRecursive(
    cart: Map<Long, Int>,
): VodovozItemUi<*> {
    val updatedItems = items.map { it.withUpdatedCartRecursive(cart) }
    return copyItem(
        cartQuantity = cart[id] ?: 0,
        items = updatedItems
    )
}

fun <T : VodovozItemUi<T>> Iterable<T>.withUpdatedFavoritesRecursive(
    favorites: Map<Long, Boolean>,
): List<T> = map { vodovozItem ->
    vodovozItem.copyItem(
        isFavorite = favorites[vodovozItem.id] ?: vodovozItem.isFavorite,
        items = vodovozItem.items.map { child ->
            child.withUpdatedFavoritesRecursive(favorites)
        }
    )
}


fun VodovozItemUi<*>.withUpdatedFavoritesRecursive(
    favorites: Map<Long, Boolean>,
): VodovozItemUi<*> {
    return copyItem(
        isFavorite = favorites[id] ?: isFavorite,
        items = items.map { it.withUpdatedFavoritesRecursive(favorites) }
    )
}

fun <T : VodovozItemUi<T>> Iterable<T>.withUpdatedLoadingsRecursive(
    loadings: Set<Long>,
): List<T> =
    map { vodovozItem ->
        vodovozItem.copyItem(
            cartLoading = vodovozItem.id in loadings,
            items = vodovozItem.items.map { child ->
                child.withUpdatedLoadingsRecursive(loadings)
            }
        )
    }

private fun VodovozItemUi<*>.withUpdatedLoadingsRecursive(
    loadings: Set<Long>,
): VodovozItemUi<*> {
    return copyItem(
        cartLoading = id in loadings,
        items = items.map { child ->
            child.withUpdatedLoadingsRecursive(loadings)
        }
    )
}

@Immutable
data class CategoryWithProductsUi(
    override val id: Long,
    val name: String,
    override val items: List<ProductUi>,
) : VodovozItemUi<CategoryWithProductsUi>() {

    companion object {
        val Empty = CategoryWithProductsUi(-1, "", emptyList())
    }

    override fun copyItem(
        forAdults: ForAdultsUi?,
        cartLoading: Boolean,
        isFavorite: Boolean,
        cartQuantity: Int,
        items: List<VodovozItemUi<*>>,
    ): CategoryWithProductsUi {
        return copy(items = items.mapNotNull { it as? ProductUi })
    }

}


fun CategoryWithProductsModel.toUi(): CategoryWithProductsUi {
    return CategoryWithProductsUi(
        id = id,
        name = name,
        items = products.map { it.toUi() }
    )
}

@Immutable
data class ButtonUi(
    val name: String,
    val action: ButtonAction,
) {
    companion object {
        val Empty = ButtonUi("", ButtonAction.Id(-1))
    }
}

fun ButtonModel.toUi(): ButtonUi {
    return ButtonUi(
        name = name,
        action = action
    )
}

@Immutable
data class VodovozSectionUi<E : VodovozItemUi<E>>(
    override val title: String,
    override val items: List<E>,
    override val button: ButtonUi?,
    override val placeholder: VodovozPlaceholderUi?,
) : SectionContentUi<E>, VodovozItemUi<VodovozSectionUi<E>>() {

    override fun copyItem(
        forAdults: ForAdultsUi?,
        cartLoading: Boolean,
        isFavorite: Boolean,
        cartQuantity: Int,
        items: List<VodovozItemUi<*>>,
    ): VodovozSectionUi<E> {
        return copy(
            items = items.mapNotNull {
                @Suppress("UNCHECKED_CAST")
                it as? E
            }
        )
    }

    fun withItems(block: List<E>.() -> List<E>): VodovozSectionUi<E> {
        return copy(items = block(items))
    }

    companion object {
        fun <E : VodovozItemUi<E>> empty() = VodovozSectionUi("", emptyList<E>(), null, null)
    }
}

@Immutable
data class SectionUi<E>(
    override val title: String,
    override val items: List<E>,
    override val button: ButtonUi? = null,
    override val placeholder: VodovozPlaceholderUi? = null,
) : SectionContentUi<E> {

    companion object {

        fun <T> empty() = SectionUi("", emptyList<T>(), null)
    }

    fun withItems(block: List<E>.() -> List<E>): SectionUi<E> {
        return copy(items = block(items))
    }
}


@Stable
interface SectionContentUi<E> {
    val title: String
    val items: List<E>
    val button: ButtonUi?
    val placeholder: VodovozPlaceholderUi?

}

fun <E, E2> SectionModel<E>.toUi(
    mapItems: (List<E>) -> List<E2>,
): SectionUi<E2> {
    return SectionUi(
        title = title,
        items = mapItems(items),
        button = button?.toUi(),
        placeholder = placeholder?.toUi()
    )
}

inline fun <E, E2 : VodovozItemUi<E2>> SectionModel<E>.mapToVodovozUi(
    crossinline map: (E) -> E2,
): VodovozSectionUi<E2> =
    VodovozSectionUi(
        title = title,
        items = items.map(map),
        button = button?.toUi(),
        placeholder = placeholder?.toUi()
    )

@JvmName("toPopularCategoryUi")
fun SectionModel<PopularCategoryModel>.toUi(): SectionUi<PopularCategoryUi> {
    return toUi { list -> list.map { it.toUi() } }
}

fun SectionModel<CategoryWithProductsModel>.toUi(): VodovozSectionUi<CategoryWithProductsUi> {
    return mapToVodovozUi { it.toUi() }

}

fun SectionModel<ProductModel>.toVodovozSectionUi(): VodovozSectionUi<ProductUi> {
    return VodovozSectionUi(
        title = title,
        items = items.mapToUi(),
        button = button?.toUi(),
        placeholder = placeholder?.toUi()
    )
}

fun SectionModel<ProductModel>.toUi(): SectionUi<ProductUi> {
    return SectionUi(
        title = title,
        items = items.mapToUi(),
        button = button?.toUi()
    )
}

@Immutable
data class ProductUi(
    val rating: Float,
    val price: Float,
    val oldPrice: Float,
    val name: String,
    val image: String,
    val labels: List<LabelUi>,
    val isAvailable: Boolean,
    val pricePerUnit: String?,
    val unitOfMeasurement: String?,
    val button: ColorfulButtonUi?,

    override val id: Long,
    override val isFavorite: Boolean,
    override val cartQuantity: Int,
    override val cartLoading: Boolean,
    override val forAdults: ForAdultsUi?,
) : VodovozItemUi<ProductUi>() {

    override fun copyItem(
        forAdults: ForAdultsUi?,
        cartLoading: Boolean,
        isFavorite: Boolean,
        cartQuantity: Int,
        items: List<VodovozItemUi<*>>,
    ): ProductUi = copy(
        forAdults = forAdults,
        cartQuantity = cartQuantity,
        isFavorite = isFavorite,
        cartLoading = cartLoading,
    )

    companion object {
        val Empty = ProductUi(
            rating = 0f,
            price = 0f,
            oldPrice = 0f,
            name = "",
            image = "",
            labels = emptyList(),
            isAvailable = false,
            pricePerUnit = null,
            unitOfMeasurement = null,
            button = null,
            id = -1L,
            isFavorite = false,
            cartQuantity = 0,
            cartLoading = false,
            forAdults = null
        )
    }
}

abstract class VodovozItemUi<out T : VodovozItemUi<T>> {

    open val id: Long = -1
    open val forAdults: ForAdultsUi? = null
    open val cartLoading: Boolean = false
    open val isFavorite: Boolean = false
    open val cartQuantity: Int = 0

    open val items: List<VodovozItemUi<*>> = emptyList()

    abstract fun copyItem(
        forAdults: ForAdultsUi? = this.forAdults,
        cartLoading: Boolean = this.cartLoading,
        isFavorite: Boolean = this.isFavorite,
        cartQuantity: Int = this.cartQuantity,
        items: List<VodovozItemUi<*>> = this.items,
    ): T
}

val ProductUi.percentLabels
    get() = labels.filter { labelEntity ->
        labelEntity.name.any { s -> s == '%' }
    }

val ProductUi.notPercentLabels get() = labels - percentLabels.toSet()


@Composable
fun ProductUi.PricePerUnitText(modifier: Modifier = Modifier) {
    val pricePerUnitText = pricePerUnit ?: ""

    Text(
        modifier = modifier,
        maxLines = 1,
        text = pricePerUnitText,
        color = MaterialTheme.colorScheme.surfaceTint,
        style = ExtendedTheme.typography.labelExtraSmallVariant.copy(
            lineHeightStyle = LineHeightStyle(
                LineHeightStyle.Alignment.Top,
                LineHeightStyle.Trim.FirstLineTop
            )
        )
    )

}


@Composable
fun ProductUi.Button(
    modifier: Modifier = Modifier,
    onAnalogsClick: (ProductUi) -> Unit,
    onIncrementToCart: (ProductUi) -> Unit,
    onDecrementToCart: (ProductUi) -> Unit,

    ) {
    val product = this@Button
    val currentView = LocalView.current

    //todo - you can delete this lambda and upgrade by clean architecture! XD
    val onAnalogsOrPreOrderClick = { it: ProductUi ->
        if (it.button?.id == "predzakaz") currentView.findNavController().navigateToPreOrder(it.id)
        else onAnalogsClick(it)
    }


    Box(modifier = modifier) {
        when {
            button != null -> {
                VodovozButtonSmall(
                    text = button.name,
                    onClick = { onAnalogsOrPreOrderClick(product) },
                    colors = VodovozButtonDefaults.colors(
                        contentColor = button.textColor,
                        containerColor = button.backgroundColor
                    )
                )
            }

            cartQuantity > 0 -> {
                QuantityButtonSmall(
                    isLoading = cartLoading,
                    quantity = cartQuantity,
                    onPlus = { onIncrementToCart(product) },
                    onMinus = { onDecrementToCart(product) }
                )
            }

            else -> {
                VodovozButtonSmall(
                    text = stringResource(id = R.string.to_cart),
                    onClick = { onIncrementToCart(product) },
                )
            }
        }
    }
}


fun List<ProductModel>.mapToUi(): List<ProductUi> {
    return mapNotNull { it.toUi() }
}

fun ProductModel.toUi(): ProductUi {
    return ProductUi(
        id = id,
        isFavorite = isFavorite,
        rating = rating,
        price = firstPrice.price,
        oldPrice = firstPrice.oldPrice,
        name = name,
        cartQuantity = cartQuantity,
        cartLoading = false,
        image = picture,
        labels = labels.toUi(),
        isAvailable = quantity > 0,
        pricePerUnit = pricePerUnit,
        unitOfMeasurement = unitOfMeasurement,
        forAdults = forAdults?.toUi(),
        button = analogButton?.toUi()
    )
}


@Immutable
data class LabelUi(
    val name: String,
    val color: Color,
    val background: Color = Color.Unspecified,
)

fun List<LabelModel>.toUi(): List<LabelUi> {
    return mapNotNull { labelModel ->
        labelModel.toUi()
    }
}

fun LabelModel.toUi(): LabelUi {
    return LabelUi(
        name,
        Color.fromHexOrUnspecified(colorHex),
        Color.fromHexOrUnspecified(backgroundHex)
    )
}
