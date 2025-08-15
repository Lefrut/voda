package com.vodovoz.app.feature.bottom.services.detail.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.ui.paging.ItemsState

@Immutable
data class ServiceDetailsState(
    val uiState: ServiceDetailsUiState = ServiceDetailsUiState.Loading,
    val title: String = "",
    val image: String = "",
    val html: String = "",
    val productsSection: ServiceProductsUi? = null,
    val button: ColorfulButtonUi? = null,
    val webViewIsLoading: Boolean = true
): ItemsState<ServiceProductsUi, ServiceDetailsState>(){

    override val items: List<ServiceProductsUi>
        get() = listOf(productsSection).mapNotNull { section -> section }

    override fun withItems(newItems: List<ServiceProductsUi>): ServiceDetailsState {
        return copy(productsSection = newItems.firstOrNull())
    }

}
