package com.m.vodovoz.util

import com.m.vodovoz.design_system.model.PriceUi
import java.math.RoundingMode
import java.text.DecimalFormat


val PRICE_FORMATTER = DecimalFormat("#,###")


fun calculateProductPrice(quantity: Int, priceList: List<PriceUi>): Float {
    if (priceList.isEmpty()) return Float.MAX_VALUE
    val priceItem =
        priceList.firstOrNull { quantity in it.quantityFrom..it.quantityTo }
            ?: priceList.lastOrNull()
    return quantity * (priceItem?.price ?: (Float.MAX_VALUE / 2))
}

fun Number.formatPrice(): String {
    return PRICE_FORMATTER.format(this)
}

fun Float.formatRoundedPrice(): String = toBigDecimal()
    .setScale(0, RoundingMode.HALF_UP)
    .toInt()
    .formatPrice()