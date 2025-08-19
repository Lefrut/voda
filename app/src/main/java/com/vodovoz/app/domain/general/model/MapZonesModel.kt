package com.vodovoz.app.domain.general.model

import com.vodovoz.app.domain.general.model.location.MapAreaModel

data class MapZonesModel(
    val areas: List<MapAreaModel>,
    val imageButton: ImageButtonModel?,
    val popupWindow: MapPopupWindowModel?
)
