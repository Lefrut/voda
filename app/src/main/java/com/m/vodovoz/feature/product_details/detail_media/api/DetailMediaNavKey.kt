package com.m.vodovoz.feature.product_details.detail_media.api

import androidx.compose.runtime.Immutable
import com.m.vodovoz.core.navigation.VodovozNavKey
import com.m.vodovoz.design_system.model.ProductMediaUi
import com.m.vodovoz.core.navigation.viewmodel.SharedViewModelStoreNavKey

@Immutable
data class DetailMediaNavKey(
    val media: ProductMediaUi,
    val mediaList: List<ProductMediaUi>,
    override val parentContentKey: String? = null,
) : VodovozNavKey, SharedViewModelStoreNavKey {
    override fun toString(): String = parentContentKey
        ?: "DetailMediaNavKey(media=$media, mediaList=$mediaList)"

    companion object {
        const val NAV_NAME: String = "feature/product_details/detail_media/DetailMedia"
    }
}
