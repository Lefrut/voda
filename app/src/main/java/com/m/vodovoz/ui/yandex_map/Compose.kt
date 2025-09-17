package com.m.vodovoz.ui.yandex_map

import androidx.compose.runtime.Stable
import com.yandex.mapkit.mapview.MapView


@Stable
data class YandexMapUi(
    val mapView: MapView
)