package com.m.vodovoz.domain.general.model.location

import com.m.vodovoz.domain.general.model.widgets.ImageButtonModel

data class MapZonesModel(
    val areas: List<MapAreaModel>,
    val imageButton: ImageButtonModel?,
    val popupWindow: MapPopupWindowModel?
)
