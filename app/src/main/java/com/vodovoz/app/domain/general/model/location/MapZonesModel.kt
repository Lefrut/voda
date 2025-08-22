package com.vodovoz.app.domain.general.model.location

import com.vodovoz.app.domain.general.model.widgets.ImageButtonModel

data class MapZonesModel(
    val areas: List<MapAreaModel>,
    val imageButton: ImageButtonModel?,
    val popupWindow: MapPopupWindowModel?
)
