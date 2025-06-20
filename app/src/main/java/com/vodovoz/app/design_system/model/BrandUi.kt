package com.vodovoz.app.design_system.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.domain.general.model.brand.BrandModel

@Immutable
data class BrandUi(
    val name: String,
    val id: Long,
    val picture: String,
    val url: String
)

fun BrandModel.toUi(): BrandUi{
    return BrandUi(
        name = name,
        id = id,
        picture = picture,
        url = pageUrl
    )
}
