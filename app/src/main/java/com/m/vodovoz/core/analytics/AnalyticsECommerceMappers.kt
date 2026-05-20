package com.m.vodovoz.core.analytics

import com.m.vodovoz.design_system.model.ProductDetailsUi
import com.m.vodovoz.design_system.model.ProductUi
import com.m.vodovoz.design_system.model.PriceUi
import com.m.vodovoz.domain.general.model.cart.CartItemModel
import com.m.vodovoz.domain.general.model.exceptions.VodovozPlaceholderModel
import io.appmetrica.analytics.ecommerce.ECommerceAmount
import io.appmetrica.analytics.ecommerce.ECommerceCartItem
import io.appmetrica.analytics.ecommerce.ECommerceEvent
import io.appmetrica.analytics.ecommerce.ECommerceOrder
import io.appmetrica.analytics.ecommerce.ECommercePrice
import io.appmetrica.analytics.ecommerce.ECommerceProduct

object AnalyticsEventNames {
    const val PRODUCT_DETAILS_VIEW_NEW = "Просмотр карточки товара NEW"
    const val PRODUCT_ADDED_TO_CART_NEW = "Добавления товара в корзину NEW"
    const val CART_OPENED_NEW = "Зашел в корзину NEW"
    const val CHECKOUT_OPENED_NEW = "Зашел на экран оформления заказа NEW"
    const val ORDER_CREATED_NEW = "Заказ оформлен NEW"
}

data class AnalyticsProduct(
    val id: Long,
    val name: String,
    val price: Float,
    val oldPrice: Float? = null,
    val cartQuantity: Int,
)

fun ProductDetailsUi.toAnalyticsProduct(): AnalyticsProduct {
    val priceForQuantity = priceForQuantity(cartQuantity)

    return AnalyticsProduct(
        id = id,
        name = name,
        price = priceForQuantity.price,
        oldPrice = priceForQuantity.oldPrice.takeIf { it > 0f },
        cartQuantity = cartQuantity,
    )
}

fun ProductUi.toAnalyticsProduct(): AnalyticsProduct {
    return AnalyticsProduct(
        id = id,
        name = name,
        price = price,
        oldPrice = oldPrice.takeIf { it > 0f },
        cartQuantity = cartQuantity
    )
}

fun CartItemModel.toAnalyticsProduct(): AnalyticsProduct {
    return AnalyticsProduct(
        id = productId,
        name = productName,
        price = currentPrice,
        oldPrice = basePrice.takeIf { it > 0f && it != currentPrice },
        cartQuantity = quantity,
    )
}

fun VodovozPlaceholderModel.extractOrderId(): String? {
    return sequenceOf(
        title,
        headerHtml,
        descriptionHtml
    ).firstNotNullOfOrNull { text -> ORDER_ID_REGEX.find(text)?.value }
}

fun AnalyticsProduct.toShowProductDetailsEvent(): ECommerceEvent {
    return ECommerceEvent.showProductDetailsEvent(toECommerceProduct(), null)
}

fun AnalyticsProduct.toAddCartItemEvent(addedQuantity: Int): ECommerceEvent {
    return ECommerceEvent.addCartItemEvent(toECommerceCartItem(addedQuantity))
}

fun AnalyticsProduct.toRemoveCartItemEvent(removedQuantity: Int): ECommerceEvent {
    return ECommerceEvent.removeCartItemEvent(toECommerceCartItem(removedQuantity))
}

fun List<AnalyticsProduct>.toPurchaseEvent(orderId: String?): ECommerceEvent? {
    val cartItems = filter { product -> product.cartQuantity > 0 && product.price > 0 }
        .map { product -> product.toECommerceCartItem() }

    return cartItems
        .takeIf { items -> items.isNotEmpty() }
        ?.let { items ->
            ECommerceEvent.purchaseEvent(
                ECommerceOrder(orderId.orderIdentifierOrFallback(), items)
            )
        }
}

private fun AnalyticsProduct.toECommerceProduct(): ECommerceProduct {
    val product = ECommerceProduct(id.toString())
        .setName(name)
        .setActualPrice(price.toECommercePrice())

    oldPrice
        ?.takeIf { price -> price > 0f }
        ?.let { price -> product.setOriginalPrice(price.toECommercePrice()) }

    return product
}

private fun AnalyticsProduct.toECommerceCartItem(quantityForEvent: Int = cartQuantity): ECommerceCartItem {
    val totalPrice = (price * quantityForEvent).toECommercePrice()
    return ECommerceCartItem(toECommerceProduct(), totalPrice, quantityForEvent.toDouble())
}

private fun Float.toECommercePrice(): ECommercePrice {
    return ECommercePrice(ECommerceAmount(toDouble(), CURRENCY_RUB))
}

private fun String?.orderIdentifierOrFallback(): String {
    return takeUnless { it.isNullOrBlank() } ?: "order_${System.currentTimeMillis()}"
}

private val ORDER_ID_REGEX = Regex("\\d+")
private const val CURRENCY_RUB = "RUB"

private fun ProductDetailsUi.priceForQuantity(quantity: Int): PriceUi {
    return prices.firstOrNull { price -> quantity in price.quantityFrom..price.quantityTo }
        ?: prices.lastOrNull()
        ?: firstPrice
}
