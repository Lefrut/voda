package com.m.vodovoz.feature.product_details.detail_media.api

import androidx.navigation3.runtime.NavKey
import com.m.vodovoz.design_system.model.ProductMediaUi

data class DetailMediaNavKey(
    val media: ProductMediaUi,
    val mediaList: List<ProductMediaUi>,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/product_details/detail_media/DetailMedia"
    }
}
