package com.vodovoz.app.feature.all.orders.detail.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.vodovoz.app.domain.general.model.order.OrderDetailsButtonModel
import com.vodovoz.app.feature.all.orders.detail.composables.AboutOrderPopupWindowUi
import com.vodovoz.app.feature.all.orders.detail.composables.toUi
import com.vodovoz.app.ui.graphics.fromHexOrUnspecified

@Stable
sealed class OrderDetailsButtonUi(
    open val id: String,
    open val name: String,
    open val isSmall: Boolean = false,
    open val popupWindow: AboutOrderPopupWindowUi? = null,
    open val url: String? = null,
    open val browser: Boolean? = null,
    open val driverId: String? = null
) {

    @Immutable
    data class ImageButton(
        override val id: String,
        override val name: String,
        val image: String,
        val backgroundColor: Color,
        val textColor: Color,
    ) : OrderDetailsButtonUi(id, name)

    @Immutable
    data class OutlineButton(
        override val id: String,
        override val name: String,
        val description: String,
        val image: String,
        override val popupWindow: AboutOrderPopupWindowUi?,
        override val url: String?,
        override val browser: Boolean?,
    ) : OrderDetailsButtonUi(id, name)

    @Immutable
    data class Button(
        override val id: String,
        override val name: String,
        val backgroundColor: Color,
        val textColor: Color,
        override val url: String?,
        override val browser: Boolean?,
        override val driverId: String?,
        override val isSmall: Boolean,
    ) : OrderDetailsButtonUi(id, name, isSmall)
}

fun List<OrderDetailsButtonModel>.mapToUi(): List<OrderDetailsButtonUi> {
    return mapNotNull { buttonModel -> buttonModel.toUi() }
}

fun OrderDetailsButtonModel.toUi(): OrderDetailsButtonUi {
    return when {
        description.isNotBlank() && image.isNotBlank() -> OrderDetailsButtonUi.OutlineButton(
            id = id,
            name = name,
            description = description,
            image = image,
            popupWindow = popupWindow?.toUi(),
            url = url,
            browser = browser
        )

        image.isNotBlank() -> OrderDetailsButtonUi.ImageButton(
            id = id,
            name = name,
            image = image,
            backgroundColor = Color.fromHexOrUnspecified(backgroundColor),
            textColor = Color.fromHexOrUnspecified(textColor)
        )

        else -> OrderDetailsButtonUi.Button(
            id = id,
            name = name,
            backgroundColor = Color.fromHexOrUnspecified(backgroundColor),
            textColor = Color.fromHexOrUnspecified(textColor),
            url = url,
            browser = browser,
            driverId = driverId,
            isSmall = isSmall
        )
    }
}
