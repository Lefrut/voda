package com.vodovoz.app.feature.bottom.services.detail.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.design_system.model.ColorfulButtonUi

@Immutable
data class ServiceDetailsState(
    val uiState: ServiceDetailsUiState = ServiceDetailsUiState.Loading,
    val title: String = "",
    val image: String = "",
    val html: String = "",
    val productsSection: ServiceProductsUi? = null,
    val button: ColorfulButtonUi? = null
)
