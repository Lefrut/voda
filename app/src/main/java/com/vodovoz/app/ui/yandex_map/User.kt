package com.vodovoz.app.ui.yandex_map

import android.content.Context
import android.graphics.PointF
import androidx.compose.ui.graphics.Color
import com.vodovoz.app.R
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.RotationType
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider

class VodovozUserLocationListener(
    private val context: Context,
) : UserLocationObjectListener {
    override fun onObjectAdded(p0: UserLocationView) {

        val imageProvider = ImageProvider.fromResource(context, R.drawable.pic_gps)

        p0.arrow.setIcon(
            imageProvider,
            IconStyle()
                .setScale(0.12f)
                .setAnchor(PointF(0.5f, 0.5f))
                .setRotationType(RotationType.ROTATE)
                .setZIndex(1f)
        )
        p0.pin.setIcon(
            imageProvider,
            IconStyle()
                .setScale(0.12f)
                .setAnchor(PointF(0.5f, 0.5f))
                .setRotationType(RotationType.ROTATE)
                .setZIndex(0f)
        )

        p0.accuracyCircle.fillColor = Color.Transparent.hashCode()
    }

    override fun onObjectRemoved(p0: UserLocationView) = Unit

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) = Unit
}