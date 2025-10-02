package com.m.vodovoz.design_system.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.m.vodovoz.domain.general.model.order.OrderProductModel
import com.m.vodovoz.domain.general.model.order.OrderProductPresentModel
import com.m.vodovoz.feature.cart.model.ProductRestrictionUi
import com.m.vodovoz.ui.graphics.fromHexOrUnspecified

@Immutable
data class OrderProductUi(
    override val id: Long,
    val name: String,
    val quantity: Int,
    val articleNumberText: String,
    val depositText: String?,
    val price: PriceUi?,
    val showcase: Boolean,
    val image: String,
    val labels: List<LabelUi>,
    val pricePerUnit: String?,
    val unitOfMeasurement: String?,
    val catalogQuantity: Int,
    override val isFavorite: Boolean,
    val present: OrderProductPresentUi?,
    val restrictions: ProductRestrictionUi,
) : VodovozItemUi<OrderProductUi>() {

    override fun copyItem(
        forAdults: ForAdultsUi?,
        cartLoading: Boolean,
        isFavorite: Boolean,
        cartQuantity: Int,
        items: List<VodovozItemUi<*>>
    ): OrderProductUi = copy(isFavorite = isFavorite)
}


@Immutable
data class OrderProductPresentUi(
    val title: String,
    val color: Color,
)

fun List<OrderProductModel>.mapToUi(): List<OrderProductUi> {
    return map { it.toUi() }
}

fun OrderProductModel.toUi(): OrderProductUi {
    return OrderProductUi(
        id = id,
        name = name,
        quantity = quantity,
        articleNumberText = articleNumberText,
        depositText = depositText,
        price = price?.toUi(),
        showcase = showcase,
        image = image,
        labels = labels.map { it.toUi() },
        pricePerUnit = pricePerUnit,
        unitOfMeasurement = unitOfMeasurement,
        catalogQuantity = catalogQuantity,
        isFavorite = isFavorite,
        present = present?.toUi(),
        restrictions = ProductRestrictionUi.fromCode(restrictionCode)
    )
}

fun OrderProductPresentModel.toUi(): OrderProductPresentUi {
    return OrderProductPresentUi(
        title, Color.fromHexOrUnspecified(color)
    )
}
