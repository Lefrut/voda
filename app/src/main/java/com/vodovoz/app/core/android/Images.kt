package com.vodovoz.app.core.android

import android.content.Context
import coil3.Bitmap
import coil3.executeBlocking
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap


fun Context.getBitmap(url: String): Bitmap? {
    val loader = imageLoader
    val request = ImageRequest.Builder(this)
        .data(url)
        .allowHardware(false)
        .build()

    val image = loader.executeBlocking(request).image
    return image?.let { it ->
        it.toBitmap(it.width, it.height)
    }
}
