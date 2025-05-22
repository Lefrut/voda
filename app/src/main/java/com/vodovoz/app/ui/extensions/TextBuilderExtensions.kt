package com.vodovoz.app.ui.extensions

import android.widget.TextView
import java.util.Locale
import kotlin.math.roundToInt

object TextBuilderExtensions {

    fun TextView.setPriceText(
        price: Int?,
        isNegative: Boolean = false,
        itCanBeGift: Boolean = false
    ) {
        if (price == null) return
        if (price == 0 && itCanBeGift) {
            text = "Подарок"
            return
        }
        this.text = StringBuilder()
            .append(when(isNegative && price != 0) {
                true -> "-"
                false -> ""
            })
            .append(price)
            .append(" ₽")
            .toString()
    }

}