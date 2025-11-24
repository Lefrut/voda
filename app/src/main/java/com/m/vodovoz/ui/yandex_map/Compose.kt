package com.m.vodovoz.ui.yandex_map

import androidx.compose.runtime.Stable
import com.yandex.mapkit.map.MapWindow
import com.yandex.mapkit.mapview.MapView


@Stable
data class YandexMapUi(
    val mapView: MapView,
) {

    val mapWindow: MapWindow get() = mapView.mapWindow
    val map get() = mapWindow.map

}