package com.vodovoz.app.feature.product_details.detail_media.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.design_system.model.ProductMediaUi

@Immutable
data class DetailMediaState(
    val currentMedia: ProductMediaUi = ProductMediaUi.Picture(""),
    val mediaList: List<ProductMediaUi> = emptyList(),
    val portraitOrientation: Boolean = true
)
