package com.m.vodovoz.feature.about_product.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.CharacteristicsBlockUi
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.ContentBlockUi
import com.m.vodovoz.design_system.model.DocumentUi
import com.m.vodovoz.design_system.model.PriceUi
import com.m.vodovoz.design_system.model.ProductDetailsTabUi
import com.m.vodovoz.design_system.model.ProductUi
import com.m.vodovoz.ui.paging.ItemsState
import com.m.vodovoz.util.calculateProductPrice
import kotlin.math.roundToInt

@Immutable
data class AboutProductState(
    val uiState: AboutProductUiState = AboutProductUiState.Success,
    val tabs: List<ProductDetailsTabUi> = emptyList(),
    val characteristics: ContentBlockUi<List<CharacteristicsBlockUi>> = ContentBlockUi(
        "",
        emptyList(),
        ""
    ),
    val documents: ContentBlockUi<List<DocumentUi>> = ContentBlockUi("", emptyList(), ""),
    val description: ContentBlockUi<String> = ContentBlockUi("", "", ""),
    val selectedTabIndex: Int = 0,

    val presentHtml: String = "",
    val analogButton: ColorfulButtonUi? = null,
    val productPrices: List<PriceUi> = emptyList(),
    override val items: List<ProductUi> = emptyList(),
) : ItemsState<ProductUi, AboutProductState>() {

    override fun withItems(newItems: List<ProductUi>): AboutProductState = copy(items = newItems)

    val product = items.firstOrNull() ?: ProductUi.Empty
    val productTotalPrice: Int
        get() {
            return calculateProductPrice(
                product.cartQuantity,
                productPrices
            ).roundToInt()
        }
}
