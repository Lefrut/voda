package com.vodovoz.app.design_system.model

import androidx.compose.runtime.Immutable

@Immutable
data class MapPointUi(
    val lat: Double,
    val lon: Double
){
    companion object{
        val Empty = MapPointUi(0.0, 0.0)
    }
}
