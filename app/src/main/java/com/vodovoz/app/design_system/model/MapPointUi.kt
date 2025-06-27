package com.vodovoz.app.design_system.model

import android.os.Parcelable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.runtime.Immutable
import com.vodovoz.app.domain.general.model.location.MapPointModel
import com.yandex.mapkit.geometry.Point
import kotlinx.parcelize.Parcelize

@Parcelize
@Immutable
data class MapPointUi(
    val lat: Double,
    val lon: Double,
): Parcelable {
    companion object {
        val Empty = MapPointUi(0.0, 0.0)

        val VectorConverter = TwoWayConverter<MapPointUi?, AnimationVector2D>(
            convertToVector = { point ->
                AnimationVector2D(
                    point?.lat?.toFloat() ?: 0f,
                    point?.lon?.toFloat() ?: 0f
                )
            },
            convertFromVector = { vector ->
                MapPointUi(vector.v1.toDouble(), vector.v2.toDouble())
            },
        )
    }
}

fun Point.toMapPoint(): MapPointUi {
    return MapPointUi(lat = latitude, lon = longitude)
}

fun MapPointModel.toUi(): MapPointUi {
    return MapPointUi(lat, lon)
}

fun MapPointUi.toDomain(): MapPointModel {
    return MapPointModel(lat, lon)
}


fun MapPointUi.toPoint(): Point {
    return Point(lat, lon)
}
