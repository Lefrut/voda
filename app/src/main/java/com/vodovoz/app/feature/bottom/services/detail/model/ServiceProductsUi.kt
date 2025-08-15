package com.vodovoz.app.feature.bottom.services.detail.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.design_system.model.ForAdultsUi
import com.vodovoz.app.design_system.model.ProductUi
import com.vodovoz.app.design_system.model.VodovozItemUi
import com.vodovoz.app.design_system.model.mapToUi
import com.vodovoz.app.domain.general.model.service.ServiceProductsModel

@Immutable
data class ServiceProductsUi(
    val title: String,
    val coefficient: Int,
    override val items: List<ProductUi>,
    val additionalProductId: String,
): VodovozItemUi<ServiceProductsUi>() {

    override fun copyItem(
        forAdults: ForAdultsUi?,
        cartLoading: Boolean,
        isFavorite: Boolean,
        cartQuantity: Int,
        items: List<VodovozItemUi<*>>,
    ): ServiceProductsUi {
        return copy(
            items = items.mapNotNull { it as? ProductUi }
        )
    }

}

fun ServiceProductsModel.toUi(): ServiceProductsUi{
    return ServiceProductsUi(
        title = title,
        coefficient = coefficient,
        items = products.mapToUi(),
        additionalProductId = additionalProductId,
    )
}