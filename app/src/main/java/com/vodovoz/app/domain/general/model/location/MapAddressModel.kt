package com.vodovoz.app.domain.general.model.location

data class MapAddressModel(
    val point: MapPointModel,
    val name: String,
    val city: String,
    val street: String,
    val house: String,
)
