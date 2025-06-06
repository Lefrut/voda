package com.vodovoz.app.domain.general.model.map

import com.vodovoz.app.domain.general.model.MapPointModel

data class MapAddressModel(
    val point: MapPointModel,
    val name: String,
    val city: String,
    val street: String,
    val house: String,
)
