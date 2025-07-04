package com.vodovoz.app.domain.general.model.cart

data class CartPromoPopupWindowModel(
    val title: String,
    val value: String,
    val fieldHint: String,
    val buttonName: String,
    val errorText: String?,
    val borderColor: String,
    val color: String,
) {
    companion object {
        val Empty = CartPromoPopupWindowModel(
            title = "",
            value = "",
            fieldHint = "",
            buttonName = "",
            errorText = null,
            borderColor = "",
            color = ""
        )
    }
}
