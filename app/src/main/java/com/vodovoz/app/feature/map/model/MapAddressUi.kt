package com.vodovoz.app.feature.map.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.design_system.model.MapPointUi

@Immutable
data class MapAddressUi(
    val point: MapPointUi,
    val name: String,
    val city: String,
    val street: String,
    val house: String,
)
