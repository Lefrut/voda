package com.vodovoz.app.design_system.model

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineHeightStyle
import com.vodovoz.app.R
import com.vodovoz.app.common.model.ButtonAction
import com.vodovoz.app.design_system.ExtendedTheme
import com.vodovoz.app.design_system.composables.button.QuantityButtonSmall
import com.vodovoz.app.design_system.composables.button.VodovozButtonDefaults
import com.vodovoz.app.design_system.composables.button.VodovozButtonSmall
import com.vodovoz.app.domain.general.model.product.ButtonModel
import com.vodovoz.app.domain.general.model.product.CategoryWithProductsModel
import com.vodovoz.app.domain.general.model.product.ProductModel
import com.vodovoz.app.domain.general.model.product.SectionModel
import com.vodovoz.app.domain.general.model.promotion.LabelModel
import com.vodovoz.app.ui.graphics.fromHexOrUnspecified

@JvmName("withUpdatedCartSectionProduct")
fun SectionUi<ProductUi>.withUpdatedCart(cart: Map<Long, Int>): SectionUi<ProductUi> {
    return copy(
        items = items.withUpdatedCartRecursive(cart).mapNotNull { it as? ProductUi }
    )
}

@JvmName("withUpdatedCartSectionCategory")
fun SectionUi<CategoryWithProductsUi>.withUpdatedCart(cart: Map<Long, Int>): SectionUi<CategoryWithProductsUi> {
    return copy(
        items = items.withUpdatedCartRecursive(cart).mapNotNull { it as? CategoryWithProductsUi }
    )
}

@JvmName("withUpdatedLoadingSectionProduct")
fun SectionUi<ProductUi>.withUpdatedLoading(blockedProductsIds: Set<Long>): SectionUi<ProductUi> {
    return copy(
        items = items.withUpdatedLoadingsRecursive(blockedProductsIds)
            .mapNotNull { it as? ProductUi })
}

@JvmName("withUpdatedLoadingSectionCategory")
fun SectionUi<CategoryWithProductsUi>.withUpdatedLoading(blockedProductsIds: Set<Long>): SectionUi<CategoryWithProductsUi> {
    return copy(
        items = items.withUpdatedLoadingsRecursive(blockedProductsIds)
            .mapNotNull { it as? CategoryWithProductsUi }
    )
}

@JvmName("withUpdatedFavoritesSectionProduct")
fun SectionUi<ProductUi>.withUpdatedFavorites(favorites: Map<Long, Boolean>): SectionUi<ProductUi> {
    return copy(
        items = items.withUpdatedFavoritesRecursive(favorites).mapNotNull { it as? ProductUi }
    )
}

fun SectionUi<CategoryWithProductsUi>.withUpdatedFavorites(favorites: Map<Long, Boolean>): SectionUi<CategoryWithProductsUi> {
    return copy(
        items = items.withUpdatedFavoritesRecursive(favorites)
            .mapNotNull { it as? CategoryWithProductsUi }
    )
}

fun CategoryWithProductsUi.withUpdatedFavorites(favorites: Map<Long, Boolean>): CategoryWithProductsUi {
    return copy(
        items = items.withUpdatedFavoritesRecursive(favorites).mapNotNull { it as? ProductUi }
    )
}

fun <T : VodovozItemUi<T>> List<T>.withUpdatedFavorites(favorites: Map<Long, Boolean>): List<T> {
    return map { product ->
        product.copyItem(isFavorite = favorites[product.id] ?: product.isFavorite)
    }
}


fun List<VodovozItemUi<*>>.withUpdatedCartRecursive(
    cart: Map<Long, Int>,
): List<VodovozItemUi<*>> {
    return map { it.withUpdatedCartRecursive(cart) }
}

fun VodovozItemUi<*>.withUpdatedCartRecursive(
    cart: Map<Long, Int>,
): VodovozItemUi<*> {
    val updatedItems = items.withUpdatedCartRecursive(cart)
    return copyItem(cartQuantity = cart[id] ?: 0, items = updatedItems)
}

fun List<VodovozItemUi<*>>.withUpdatedFavoritesRecursive(
    favorites: Map<Long, Boolean>,
): List<VodovozItemUi<*>> = map { it.withUpdatedFavoritesRecursive(favorites) }

fun VodovozItemUi<*>.withUpdatedFavoritesRecursive(
    favorites: Map<Long, Boolean>,
): VodovozItemUi<*> {
    val updatedItems = items.withUpdatedFavoritesRecursive(favorites)
    return copyItem(isFavorite = favorites[id] ?: isFavorite, items = updatedItems)
}

fun List<VodovozItemUi<*>>.withUpdatedLoadingsRecursive(
    loadings: Set<Long>,
): List<VodovozItemUi<*>> = map { it.withUpdatedLoadingsRecursive(loadings) }

fun VodovozItemUi<*>.withUpdatedLoadingsRecursive(
    loadings: Set<Long>,
): VodovozItemUi<*> {
    val updatedItems = items.withUpdatedLoadingsRecursive(loadings)
    return copyItem(cartLoading = id in loadings, items = updatedItems)
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
    val pricePerUnit: Int?,
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

abstract class VodovozItemUi<T : VodovozItemUi<T>> {

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
    val pricePerUnitText =
        if (pricePerUnit != null && unitOfMeasurement != null) stringResource(
            R.string.unit_of_measurement,
            pricePerUnit,
            unitOfMeasurement
        )
        else ""

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
    isLoading: Boolean,
    onAnalogsClick: (ProductUi) -> Unit,
    onIncrementToCart: (ProductUi) -> Unit,
    onDecrementToCart: (ProductUi) -> Unit,
) {
    val product = this@Button

    Box(modifier = modifier) {
        when {
            button != null -> {
                VodovozButtonSmall(
                    text = button.name,
                    onClick = { onAnalogsClick(product) },
                    colors = VodovozButtonDefaults.colors(
                        contentColor = button.textColor,
                        containerColor = button.backgroundColor
                    )
                )
            }

            cartQuantity > 0 -> {
                QuantityButtonSmall(
                    isLoading = isLoading,
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
