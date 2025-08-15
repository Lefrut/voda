package com.vodovoz.app.util

import com.vodovoz.app.design_system.model.PriceUi
import java.text.DecimalFormat


val PRICE_FORMATTER = DecimalFormat("#,###")


fun calculateProductPrice(quantity: Int, priceList: List<PriceUi>): Float {
    if (priceList.isEmpty()) return Float.MAX_VALUE
    val priceItem =
        priceList.firstOrNull { quantity in it.quantityFrom..it.quantityTo } ?: priceList.lastOrNull()
    return quantity * (priceItem?.price ?: (Float.MAX_VALUE / 2))
}

fun Number.formatPrice(): String {
    return PRICE_FORMATTER.format(this)
}