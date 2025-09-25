package com.m.vodovoz.domain.general.model.location

data class MapPointModel(
    val lat: Double,
    val lon: Double,
) {
    companion object {
        val Empty = MapPointModel(1.0, 1.0)
    }
}
