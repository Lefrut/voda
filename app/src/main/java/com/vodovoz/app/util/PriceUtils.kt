package com.vodovoz.app.util

import com.vodovoz.app.design_system.model.PriceUi
import java.text.DecimalFormat


val PRICE_FORMATTER = DecimalFormat("#,###")


fun calculateProductPrice(quantity: Int, priceList: List<PriceUi>): Float {
    if (priceList.isEmpty()) return 0f
    val priceItem =
        priceList.firstOrNull { quantity in it.quantityFrom..it.quantityTo } ?: priceList.last()
    return quantity * priceItem.price
}

fun Number.formatPrice(): String {
    return PRICE_FORMATTER.format(this)
}