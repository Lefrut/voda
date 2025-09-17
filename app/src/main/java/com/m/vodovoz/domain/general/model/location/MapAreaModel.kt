package com.m.vodovoz.domain.general.model.location

data class MapAreaModel(
    val id: Int,
    val name: String,
    val isMoscowRingRow: Boolean,
    val color: String,
    val points: List<MapPointModel>
)
