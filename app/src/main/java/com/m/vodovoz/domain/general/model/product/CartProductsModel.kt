package com.m.vodovoz.domain.general.model.product

import com.m.vodovoz.common.cart.AbstractCartManager
import okhttp3.internal.toLongOrDefault

@JvmInline
value class CartProductsModel(
    val productsIdsWithQuantity: String,
)

fun <T : Number, T2 : Number> Map<T, T2>.toCartProducts(): CartProductsModel {
    return CartProductsModel(entries.joinToString(";") { "${it.key}-${it.value}" })
}

fun CartProductsModel.toMap(): Map<Long, Int> {
    return productsIdsWithQuantity
        .split(";")
        .filter { it.isNotBlank() }.associate {
            val (id, count) = it.split("-")
            id.toLongOrDefault(-1) to (count.toIntOrNull() ?: 1)
        }
}