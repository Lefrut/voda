package com.vodovoz.app.feature.cart.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.vodovoz.app.domain.general.model.cart.CartPromoPopupWindowModel
import com.vodovoz.app.ui.graphics.fromHexOrUnspecified


@Immutable
data class CartPromoPopupWindowUi(
    val title: String,
    val fieldHint: String,
    val buttonName: String,
    val errorText: String? = null,
    val borderColor: Color,
    val color: Color,
    val value: String,
    val buttonIsLoading: Boolean = false
) {
    companion object {
        val Empty = CartPromoPopupWindowUi(
            title = "",
            fieldHint = "",
            buttonName = "",
            errorText = null,
            borderColor = Color.Unspecified,
            color = Color.Unspecified,
            value = ""
        )
    }
}

fun CartPromoPopupWindowModel.toUi(): CartPromoPopupWindowUi {
    return CartPromoPopupWindowUi(
        title = title,
        fieldHint = fieldHint,
        buttonName = buttonName,
        errorText = errorText,
        borderColor = Color.fromHexOrUnspecified(borderColor),
        color = Color.fromHexOrUnspecified(color),
        value = value
    )
}
