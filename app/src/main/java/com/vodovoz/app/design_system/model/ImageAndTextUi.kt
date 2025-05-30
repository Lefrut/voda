package com.vodovoz.app.design_system.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.domain.general.model.ImageAndTextModel

@Immutable
data class ImageAndTextUi(
    val image: String,
    val text: String
)

fun List<ImageAndTextModel>.mapToUi(): List<ImageAndTextUi> {
    return map { it.toUi() }
}

fun ImageAndTextModel.toUi(): ImageAndTextUi {
    return ImageAndTextUi(
        image = image,
        text = text
    )
}

