package com.vodovoz.app.design_system.model

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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


@JvmName("withUpdatedFavoritesSectionProduct")
fun SectionUi<ProductUi>.withUpdatedFavorites(favorites: Map<Long, Boolean>): SectionUi<ProductUi> {
    return copy(
        items = items.withUpdatedFavorites(favorites)
    )
}

fun SectionUi<CategoryWithProductsUi>.withUpdatedFavorites(favorites: Map<Long, Boolean>): SectionUi<CategoryWithProductsUi> {
    return copy(
        items = items.withUpdatedFavorites(favorites)
    )
}

@JvmName("withUpdatedFavoritesCategoriesWithProducts")
fun List<CategoryWithProductsUi>.withUpdatedFavorites(favorites: Map<Long, Boolean>): List<CategoryWithProductsUi> {
    return map { categoryWithProductsUi ->
        categoryWithProductsUi.withUpdatedFavorites(favorites)
    }
}

fun CategoryWithProductsUi.withUpdatedFavorites(favorites: Map<Long, Boolean>): CategoryWithProductsUi {
    return copy(products = products.withUpdatedFavorites(favorites))
}

fun List<ProductUi>.withUpdatedFavorites(favorites: Map<Long, Boolean>): List<ProductUi> {
    return map { product ->
        product.copy(isFavorite = favorites[product.id] ?: product.isFavorite)
    }
}

@JvmName("withUpdatedCartSectionProduct")
fun SectionUi<ProductUi>.withUpdatedCart(cart: Map<Long, Int>): SectionUi<ProductUi> {
    return copy(
        items = items.withUpdatedCart(cart)
    )
}

@JvmName("withUpdatedCartSectionCategory")
fun SectionUi<CategoryWithProductsUi>.withUpdatedCart(cart: Map<Long, Int>): SectionUi<CategoryWithProductsUi> {
    return copy(
        items = items.withUpdatedCart(cart)
    )
}

@JvmName("withUpdatedCartCategoriesWithProducts")
fun List<CategoryWithProductsUi>.withUpdatedCart(cart: Map<Long, Int>): List<CategoryWithProductsUi> {
    return map { categoryWithProductsUi ->
        categoryWithProductsUi.withUpdatedCart(cart)
    }
}

@JvmName("withUpdatedCartCategoryWithProducts")
fun CategoryWithProductsUi.withUpdatedCart(cart: Map<Long, Int>): CategoryWithProductsUi {
    return copy(products = products.withUpdatedCart(cart))
}

@JvmName("withUpdatedCartProductList")
fun List<ProductUi>.withUpdatedCart(cart: Map<Long, Int>): List<ProductUi> {
    return map { product ->
        product.copy(cartQuantity = cart[product.id] ?: 0)
    }
}

@JvmName("withUpdatedLoadingProductList")
fun List<ProductUi>.withUpdatedLoading(blockedProductsIds: Set<Long>): List<ProductUi> {
    return map { product ->
        product.copy(cartLoading = product.id in blockedProductsIds)
    }
}

@JvmName("withUpdatedLoadingCategoryWithProducts")
fun CategoryWithProductsUi.withUpdatedLoading(blockedProductsIds: Set<Long>): CategoryWithProductsUi {
    return copy(products = products.withUpdatedLoading(blockedProductsIds))
}

@JvmName("withUpdatedLoadingCategoriesWithProducts")
fun List<CategoryWithProductsUi>.withUpdatedLoading(blockedProductsIds: Set<Long>): List<CategoryWithProductsUi> {
    return map { it.withUpdatedLoading(blockedProductsIds) }
}

@JvmName("withUpdatedLoadingSectionProduct")
fun SectionUi<ProductUi>.withUpdatedLoading(blockedProductsIds: Set<Long>): SectionUi<ProductUi> {
    return copy(items = items.withUpdatedLoading(blockedProductsIds))
}

@JvmName("withUpdatedLoadingSectionCategory")
fun SectionUi<CategoryWithProductsUi>.withUpdatedLoading(blockedProductsIds: Set<Long>): SectionUi<CategoryWithProductsUi> {
    return copy(items = items.withUpdatedLoading(blockedProductsIds))
}


@Immutable
data class CategoryWithProductsUi(
    val id: Long,
    val name: String,
    val products: List<ProductUi>,
) {

    companion object {
        val Empty = CategoryWithProductsUi(-1, "", emptyList())
    }

}


fun CategoryWithProductsModel.toUi(): CategoryWithProductsUi {
    return CategoryWithProductsUi(
        id = id,
        name = name,
        products = products.map { it.toUi() }
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
data class SectionUi<E>(
    val title: String,
    val items: List<E>,
    val button: ButtonUi? = null,
    val placeholder: VodovozPlaceholderUi? = null,
) {

    companion object {

        fun <T> empty() = SectionUi("", emptyList<T>(), null)
    }

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

fun SectionModel<ProductModel>.toUi(): SectionUi<ProductUi> {
    return SectionUi(
        title = title,
        items = items.mapToUi(),
        button = button?.toUi()
    )
}

@Immutable
data class ProductUi(
    val id: Long,
    val isFavorite: Boolean,
    val rating: Float,
    val price: Float,
    val oldPrice: Float,
    val name: String,
    val cartQuantity: Int,
    val cartLoading: Boolean,
    val image: String,
    val labels: List<LabelUi>,
    val isAvailable: Boolean,
    val pricePerUnit: Int?,
    val unitOfMeasurement: String?,
    val forAdults: ForAdultsUi?,
    val button: ColorfulButtonUi?,
)


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
