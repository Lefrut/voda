package com.m.vodovoz.common.cart

import com.m.vodovoz.core.analytics.Analytics
import com.m.vodovoz.core.analytics.AnalyticsEventNames
import com.m.vodovoz.core.analytics.AnalyticsEvents
import com.m.vodovoz.core.analytics.toAddCartItemEvent
import com.m.vodovoz.core.analytics.toAnalyticsProduct
import com.m.vodovoz.design_system.model.ProductDetailsUi
import com.m.vodovoz.design_system.model.ProductUi
import com.m.vodovoz.feature.cart.model.CartItemUi
import kotlinx.coroutines.Job

fun CartManager.change(
    product: ProductUi,
    count: Int,
    onSuccess: (CartItemQuantityChange) -> Unit = {},
    onFailure: (Throwable) -> Unit = {},
): Job {
    return change(
        productId = product.id,
        count = count,
        onSuccess = { change ->
            Analytics.reportCartItemAllEvents(product, change)
            onSuccess(change)
        },
        onFailure = onFailure,
    )
}

fun <T : Any> CartManager.addMultiple(
    cartItems: T,
    product: ProductUi,
    onSuccess: (Map<Long, CartItemQuantityChange>) -> Unit = {},
    onFailure: (Throwable) -> Unit = {},
): Job {
    return addMultiple(
        cartItems = cartItems,
        onSuccess = { map ->
            Analytics.reportCartItemAllEvents(product, map[product.id])
            onSuccess(map)
        },
        onFailure = onFailure,
    )
}

private fun AnalyticsEvents.reportCartItemAllEvents(
    product: ProductUi,
    change: CartItemQuantityChange?
) {
    change?.let { change ->
        reportCartItemEvent(product, change)
        if (change.previousCount == 0 && change.delta > 0) {
            reportEvent(AnalyticsEventNames.PRODUCT_ADDED_TO_CART_NEW)
        }
    }
}

fun AnalyticsEvents.reportCartItemEvent(
    product: ProductUi,
    change: CartItemQuantityChange
) {
    val analyticsProduct = product.toAnalyticsProduct()

    val event = when {
        change.delta > 0 && change.previousCount == 0 -> {
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
