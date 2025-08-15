package com.vodovoz.app.feature.product_details.detail_media.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.vodovoz.app.ui.mvi.collectAsState
import coil3.compose.rememberAsyncImagePainter

import com.vodovoz.app.design_system.composables.zoom.ZoomableContainer
import com.vodovoz.app.design_system.composables.zoom.rememberZoomableState

@Composable
internal fun DetailMediaImage(modifier: Modifier = Modifier, image: String) {
    val painter = rememberAsyncImagePainter(
        model = image,
        contentScale = ContentScale.Inside
    )

    Box(modifier = modifier) {
        val state = rememberZoomableState(contentSize = painter.intrinsicSize)

        ZoomableContainer(state = state) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Inside
            )
        }
    }
}