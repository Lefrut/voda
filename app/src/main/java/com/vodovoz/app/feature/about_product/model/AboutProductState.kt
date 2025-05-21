package com.vodovoz.app.feature.about_product.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.design_system.model.CharacteristicsBlockUi
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.ContentBlockUi
import com.vodovoz.app.design_system.model.DocumentUi
import com.vodovoz.app.design_system.model.ProductDetailsTabUi

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

    val buttonIsLoading: Boolean = true,
    val cartQuantity: Int = 0,

    val productTotalPrice: Int = 0,
    val productPrice: Int = 0,
    val productOldPrice: Int = 0,
    val productAvailable: Boolean = true,
    val presentHtml: String = "",
    val analogButton: ColorfulButtonUi? = null,
)
