package com.m.vodovoz.domain.general.model.location

data class MapAddressModel(
    val point: MapPointModel,
    val name: String,
    val city: String,
    val street: String,
    val house: String,
    val fromMoscowToPoint: String = "",
) {
    companion object {
        val Empty = MapAddressModel(
            point = MapPointModel.Empty,
            name = "",
            city = "",
            street = "",
            house = ""
        )
    }
}
