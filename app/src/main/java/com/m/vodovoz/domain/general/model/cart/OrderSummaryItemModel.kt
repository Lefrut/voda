package com.m.vodovoz.domain.general.model.cart

data class OrderSummaryItemModel(
    val name: String,
    val color: String = "",
    val value: String,
) {
    companion object {
        val Empty = OrderSummaryItemModel("", "", "")
    }
}
