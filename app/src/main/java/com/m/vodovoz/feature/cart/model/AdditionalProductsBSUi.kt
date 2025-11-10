package com.m.vodovoz.feature.cart.model

import androidx.compose.runtime.Immutable
import androidx.paging.CombinedLoadStates
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.ProductUi
import com.m.vodovoz.design_system.model.mapToUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.domain.general.model.cart.AdditionalProductsBSModel
import com.m.vodovoz.ui.paging.emptyCombinedLoadStates

@Immutable
data class AdditionalProductsBSUi(
    val title: String,
    val description: String,
    val products: List<ProductUi>,
    val loadStates: CombinedLoadStates,
    val button: ColorfulButtonUi?,
)

fun AdditionalProductsBSModel.toUi(): AdditionalProductsBSUi {
    return AdditionalProductsBSUi(
        title = title,
        description = description,
        products = products.mapToUi(),
        loadStates = emptyCombinedLoadStates,
        button = button?.toUi()
    )
}
