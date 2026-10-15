package com.m.vodovoz.common.cart

import com.m.vodovoz.core.analytics.Analytics
import com.m.vodovoz.core.analytics.AnalyticsEventNames
import com.m.vodovoz.core.analytics.AnalyticsEvents
import com.m.vodovoz.core.analytics.toAddCartItemEvent
import com.m.vodovoz.core.analytics.toAnalyticsProduct
import com.m.vodovoz.design_system.model.ProductDetailsUi
import com.m.vodovoz.design_system.model.ProductUi
import com.m.vodovoz.feature.cart.model.CartItemUi

suspend fun AppCartManager.setQuantity(
    product: ProductUi,
    quantity: Int,
    mode: DispatchMode = DispatchMode.DEBOUNCED,
    requestKind: RequestKind = RequestKind.ADD_OR_CHANGE,
): CartItemChange {
    val change = setQuantity(
        id = product.id,
        quantity = quantity,
        mode = mode,
        requestKind = requestKind,
    )
    Analytics.reportCartItemAllEvents(product, change)
    return change
}

suspend fun AppCartManager.adjustQuantity(
    product: ProductUi,
    delta: Int,
    mode: DispatchMode = DispatchMode.DEBOUNCED,
    requestKind: RequestKind = RequestKind.ADD_OR_CHANGE,
): CartItemChange {
    val change = adjustQuantity(
        id = product.id,
        delta = delta,
        mode = mode,
        requestKind = requestKind,
    )
    Analytics.reportCartItemAllEvents(product, change)
    return change
}

suspend fun AppCartManager.setQuantities(
    targets: Map<Long, Int>,
    product: ProductUi,
    mode: DispatchMode = DispatchMode.DEBOUNCED,
    requestKind: RequestKind = RequestKind.ADD_OR_CHANGE,
): CartChangeResult {
    val result = setQuantities(
        targets = targets,
        mode = mode,
        requestKind = requestKind,
    )
    Analytics.reportCartItemAllEvents(product, result.item(product.id))
    return result
}

suspend fun AppCartManager.incrementProducts(
    deltas: Map<Long, Int>,
    product: ProductUi,
    mode: DispatchMode = DispatchMode.DEBOUNCED,
    requestKind: RequestKind = RequestKind.ADD_MULTIPLE,
): CartChangeResult {
    val result = incrementProducts(
        deltas = deltas,
        mode = mode,
        requestKind = requestKind,
    )
    Analytics.reportCartItemAllEvents(product, result.item(product.id))
    return result
}

private fun AnalyticsEvents.reportCartItemAllEvents(
    product: ProductUi,
    change: CartItemChange,
) {
    val applied = change as? CartItemChange.Applied ?: return
    reportCartItemEvent(product, applied)
    if (applied.previousQuantity == 0 && applied.quantity > 0) {
        reportEvent(AnalyticsEventNames.PRODUCT_ADDED_TO_CART_NEW)
    }
}

fun AnalyticsEvents.reportCartItemEvent(
    product: ProductUi,
    change: CartItemChange.Applied
) {
    val analyticsProduct = product.toAnalyticsProduct()

    val event = when {
        change.quantity > 0 && change.previousQuantity == 0 -> {
            analyticsProduct.toAddCartItemEvent(1)
        }

        else -> {
            return
        }
    }

    reportEcommerce(event)
}

fun ProductDetailsUi.toProductUi(): ProductUi {
    val analyticsProduct = toAnalyticsProduct()
    return ProductUi(
        id = id,
        name = name,
        price = analyticsProduct.price,
        oldPrice = analyticsProduct.oldPrice ?: 0f,
        rating = rating,
        image = detailPicture,
        labels = labels,
        isAvailable = isAvailable,
        pricePerUnit = pricePerUnit,
        unitOfMeasurement = null,
        button = null,
        isFavorite = isFavorite,
        cartQuantity = cartQuantity,
        cartLoading = false,
        forAdults = null,
        maxQuantity = Int.MAX_VALUE
    )
}

fun CartItemUi.toProductUi(): ProductUi {
    return ProductUi(
        id = id,
        name = productName,
        price = currentPrice,
        oldPrice = basePrice,
        rating = 0f,
        image = image,
        labels = listOfNotNull(label),
        isAvailable = canBuy,
        maxQuantity = leftItems,
        pricePerUnit = null,
        unitOfMeasurement = null,
        button = null,
        isFavorite = isFavorite,
        cartQuantity = cartQuantity,
        cartLoading = cartLoading,
        forAdults = forAdults,
    )
}
