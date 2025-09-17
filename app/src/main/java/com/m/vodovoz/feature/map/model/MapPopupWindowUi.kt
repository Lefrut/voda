package com.m.vodovoz.feature.map.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.ImageAndTextUi
import com.m.vodovoz.design_system.model.mapToUi
import com.m.vodovoz.domain.general.model.location.MapPopupWindowModel

@Immutable
data class MapPopupWindowUi(
    val title: String,
    val description: String,
    val items: List<ImageAndTextUi>
)

fun MapPopupWindowModel.toUi(): MapPopupWindowUi{
    return MapPopupWindowUi(title, description, items.mapToUi())
}
